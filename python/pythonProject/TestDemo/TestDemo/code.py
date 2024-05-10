import json
from flask import Flask, request, jsonify
import pandas as pd


import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from flask_cors import CORS
from sklearn.linear_model import LogisticRegression
from sklearn.discriminant_analysis import LinearDiscriminantAnalysis
from sklearn.neighbors import KNeighborsClassifier
from sklearn.tree import DecisionTreeClassifier
from sklearn.ensemble import RandomForestClassifier
from sklearn.ensemble import GradientBoostingClassifier
from sklearn.svm import SVC
from sklearn.neural_network import MLPClassifier, MLPRegressor


#读取数据
#from fontTools.misc.symfont import y

def trianXY(type):
    data=pd.read_csv('train.csv') #读取训练集数据
    data2=pd.read_csv('test.csv') #读取测试集数据
#print(data.head(5))
#print(data2.head(5))
    X=data.copy()
    test=data2.copy()
    X.info()
    test.info()
    y=data['adjustedclose']
    data.drop('adjustedclose', axis=1, inplace=True)

    ID=data2['date']
    dist_cols = 1
    dist_rows = len(data2.columns)
    plt.figure(figsize=(6 * dist_cols, 6 * dist_rows))
    i = 1

    ax = plt.subplot(dist_rows, dist_cols, 1)
    ax = sns.kdeplot(data["date"], color="Red", fill=True)
    ax = sns.kdeplot(data2["date"], color="Blue", fill=True)
    ax.set_xlabel("date")
    ax.set_ylabel("Frequency")
    ax = ax.legend(["train", "test"])
# plt.show()
    ax = plt.subplot(dist_rows, dist_cols, 2)
    ax = sns.kdeplot(data["open"], color="Red", fill=True)
    ax = sns.kdeplot(data2["open"], color="Blue", fill=True)
    ax.set_xlabel("open")
    ax.set_ylabel("Frequency")
    ax = ax.legend(["train", "test"])
# plt.show()
    ax = plt.subplot(dist_rows, dist_cols,3)
    ax = sns.kdeplot(data["high"], color="Red", fill=True)
    ax = sns.kdeplot(data2["high"], color="Blue", fill=True)
    ax.set_xlabel("high")
    ax.set_ylabel("Frequency")
    ax = ax.legend(["train", "test"])
# plt.show()
    ax = plt.subplot(dist_rows, dist_cols,4)
    ax = sns.kdeplot(data["low"], color="Red", fill=True)
    ax = sns.kdeplot(data2["low"], color="Blue", fill=True)
    ax.set_xlabel("low")
    ax.set_ylabel("Frequency")
    ax = ax.legend(["train", "test"])
# plt.show()
    ax = plt.subplot(dist_rows, dist_cols,4)
    ax = sns.kdeplot(data["close"], color="Red", fill=True)
    ax = sns.kdeplot(data2["close"], color="Blue", fill=True)
    ax.set_xlabel("close")
    ax.set_ylabel("Frequency")
    ax = ax.legend(["train", "test"])
# plt.show()
    ax = plt.subplot(dist_rows, dist_cols,4)
    ax = sns.kdeplot(data["volume"], color="Red", fill=True)
    ax = sns.kdeplot(data2["volume"], color="Blue", fill=True)
    ax.set_xlabel("volume")
    ax.set_ylabel("Frequency")
    ax = ax.legend(["train", "test"])
# plt.show()
    corr = plt.subplots(figsize = (8,6))
    corr= sns.heatmap(data.corr(method='spearman'),annot=True,square=True)

    from sklearn.model_selection import train_test_split

    X_train, X_val, y_train, y_val = train_test_split(X, y, test_size=0.2, stratify=None, random_state=0)

    from sklearn.preprocessing import StandardScaler
    scaler = StandardScaler()
    scaler.fit(X_train)
    X_train_s = scaler.transform(X_train)
    X_val_s = scaler.transform(X_val)
    test_s = scaler.transform(test)
    df = pd.DataFrame(columns=['date','adjustedclose'])
    df['date']=ID
# 逻辑回归
    model1 = LogisticRegression(C=1e10)
# 线性判别分析
    model2 = LinearDiscriminantAnalysis()
# K近邻
    model3 = KNeighborsClassifier(n_neighbors=10)
# 决策树
    model4 = DecisionTreeClassifier(random_state=77)
# 随机森林
    model5 = RandomForestClassifier(n_estimators=1000, max_features='sqrt', random_state=10)
# 梯度提升
    model6 = GradientBoostingClassifier(random_state=123)
# 支持向量机
    model7 = SVC(kernel="rbf", random_state=77)
# 神经网络
    model8 = MLPClassifier(hidden_layer_sizes=(16, 8), random_state=77, max_iter=10000)
    model_list = [model1, model2, model3, model4, model5, model6,model7, model8]
    model_name = ['逻辑回归', '线性判别', 'K近邻', '决策树', '随机森林', '梯度提升', '支持向量机', '神经网络']
    model_C=model_list[type]
    name=model_name[type]
    model_C.fit(X_train, y_train.astype('str'))
    pred = model_C.predict(test_s)
    print(pred)
    df['adjustedclose']=pred
    # csv_name=name+'的预测结果.csv'
    # df.to_csv(csv_name,index=False)
    return df



app = Flask(__name__)
CORS(app) # 允许跨域请求
# http://127.0.0.1:5000/train
@app.route("/train", methods=["POST"])
def train():
    data = request.json.get("type")
    print(data)
    preds = trianXY(data)
    data_str = preds.to_json(orient='split',force_ascii=False)
    print(data_str)
    # 逻辑回归

    return jsonify({"code":200,'data':json.loads(data_str)})

if __name__ == '__main__':
    app.run(host="127.0.0.1", port=5000)