import json
from flask import Flask, request, jsonify
import pandas as pd


import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from flask_cors import CORS
from pandas.core.common import random_state
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

def trianXY(type,train,test):
    data=pd.json_normalize(train) #读取训练集数据
    data2=pd.json_normalize(test) #读取测试集数据
#print(data.head(5))
#print(data2.head(5))
    X=data.copy()
    test=data2.copy()
    X.info()
    test.info()
    y=data['adjustedclose']
    data.drop('adjustedclose', axis=1, inplace=True)

    ID=data2['date']

    from sklearn.model_selection import train_test_split

    X_train, X_val, y_train, y_val = train_test_split(X, y, test_size=0.2,random_state=35)

    from sklearn.preprocessing import StandardScaler
    scaler_x = StandardScaler()
    scaler_x.fit(X_train)
    scaler_y=StandardScaler()
    # scaler_y.fit(y_train.values.reshape(-1,1))
    # Y_train = scaler_y.transform(y_train.values.reshape(-1,1))
    # Y_test= scaler_y.transform(y_val.values.reshape(-1,1))
    x_train = scaler_x.transform(X_train)
    scaler_test =StandardScaler()
    test_s = scaler_x.transform(test)

    #
    # test_s = scaler.transform(x_val)
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
    model8 = MLPClassifier(hidden_layer_sizes=(16, 8),alpha=1e-5,solver='sgd', random_state=77, max_iter=10000)
#bp神经网络
    model9 = MLPRegressor(hidden_layer_sizes=(10,), random_state=10, learning_rate_init=0.1)
    model_list = [model1, model2, model3, model4, model5, model6,model7, model8,model9]
    model_C=model_list[type]
    # model_C.fit(x_train.astype('int'), y_train.astype('float32'))
    model_C.fit(data.astype('int'),data.astype('int'))
    # pred = model_C.predict(test_s)
    pred=model_C.predict(data2)
    print(pred)
    df['adjustedclose']=pred
    # csv_name=name+'的预测结果.csv'
    # df.to_csv(csv_name,index=False)
    return df

def DNN(train,test):
    import keras
    import numpy as np
    import matplotlib.pyplot as plt

    return ''

app = Flask(__name__)
CORS(app) # 允许跨域请求
# http://127.0.0.1:5000/train
@app.route("/train", methods=["POST"])
def train():
    data = request.json.get("type")
    train = request.json.get("train")
    test = request.json.get("test")
    # print(data)
    # print(test)
    # print(train)
    preds = trianXY(data,train,test)
    data_str = preds.to_json(orient='split',force_ascii=False)
    # 逻辑回归
    return jsonify({"code":200,'data':json.loads(data_str)})

if __name__ == '__main__':
    app.run(host="127.0.0.1", port=5000)