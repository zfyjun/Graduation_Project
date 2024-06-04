import json

import joblib
import numpy as np
import pandas as pd
import pymysql
import torch
import torch.nn as nn
from flask import Flask, jsonify, request
from flask_cors import CORS
from sklearn.cluster import KMeans
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split
from sklearn.neural_network import MLPClassifier
from sklearn.preprocessing import StandardScaler
from sqlalchemy import create_engine

pymysql.install_as_MySQLdb()
engine = create_engine('mysql://root:root@localhost:3306/test?charset=utf8')


app = Flask(__name__)
CORS(app) # 允许跨域请求


# 创建处理函数
def default(o):
    if isinstance(o, np.integer): return int(o)
    raise TypeError

@app.route('/user_analyze', methods=['POST'])
def get_data():
    # 读取csv文件，将其读入一个 pandas DataFrame
    url = "E:/CSV/train_set.csv"
    df = pd.read_csv(url)

    # 使用年龄和余额对客户进行分群
    X = df[['age', 'balance']]

    # 使用KMeans找和合适的客户群数
    wcss = []
    for i in range(1, 11):
        kmeans = KMeans(n_clusters=i, init='k-means++', max_iter=300, n_init=10, random_state=0)
        kmeans.fit(X)
        wcss.append(kmeans.inertia_)

    # 根据Elboe Method的结果选择合适的簇数，然后用K-means算法进行分群
    n_clusters = 5  # 假设我们选择了5
    kmeans = KMeans(n_clusters=n_clusters, init='k-means++', max_iter=300, n_init=10, random_state=0)
    y_kmeans = kmeans.fit_predict(X)

    clusters = []
    for i in range(n_clusters):
        cluster_i = X[y_kmeans == i][['age', 'balance']].values.tolist()
        clusters.append(cluster_i)
    # centers = kmeans.cluster_centers_.tolist()
    # 以JSON格式返回数据
    # print(jsonify({'clusters': clusters}))
    return jsonify({'clusters': clusters})
    # return jsonify({'clusters': clusters, 'centers': centers})


@app.route('/user_age', methods=['POST'])
def get_age():
    url = "E:/CSV/train_set.csv"
    data = pd.read_csv(url)

    name = ['age', 'job', 'balance', 'marital', 'education']

    # 计算年龄的分布频数
    age_data = data['age'].value_counts().sort_index().reset_index()
    age_data.columns = ['age', 'count']
    age_data_dict = age_data.to_dict(orient='records')

    # 进行描述性统计分析
    describe_age = data['age'].describe().to_dict()

    # 计算工作类型的分布频数
    job_data = data['job'].value_counts().reset_index()
    job_data.columns = ['job', 'count']
    job_data_dict = job_data.to_dict(orient='records')

    tmp1 = data['job'].describe().to_dict()
    describe_job = json.dumps(tmp1, default=default)

    # 计算余额的分布频数
    balance_data = data['balance'].value_counts().sort_index().to_frame().reset_index()
    balance_data.columns = ['balance', 'count']
    balance_data_dict = balance_data.to_dict(orient='records')

    describe_balance = data['balance'].describe().to_dict()

    # 计算教育的分布频数
    education_data = data['education'].value_counts().sort_index().to_frame().reset_index()
    education_data.columns = ['education', 'count']
    education_data_dict = education_data.to_dict(orient='records')

    tmp3 = data['education'].describe().to_dict()
    describe_education = json.dumps(tmp3, default=default)

    # 计算婚姻的分布频数
    marital_data = data['marital'].value_counts().sort_index().to_frame().reset_index()
    marital_data.columns = ['marital', 'count']
    marital_data_dict = marital_data.to_dict(orient='records')

    tmp4 = data['marital'].describe().to_dict()
    describe_marital = json.dumps(tmp4, default=default)

    # 返回数据给前端
    return jsonify([{'name': 'age', 'age': age_data_dict, 'describe': describe_age},
                    {'name': 'job', 'job': job_data_dict, 'describe': describe_job},
                    {'name': 'balance', 'balance': balance_data_dict, 'describe': describe_balance},
                    {'name': 'education', 'education': education_data_dict, 'describe': describe_education},
                    {'name': 'marital', 'marital': marital_data_dict, 'describe': describe_marital}])

@app.route('/train',methods=['POST'])
def train():
    # 载入数据
    url = "E:/CSV/train_set.csv"
    data = pd.read_csv(url)
    X = data[['age', 'balance', 'day', 'duration', 'campaign', 'pdays']]
    y = data['y']
    scaler = StandardScaler()
    X = scaler.fit_transform(X)

    # 将“Y”中的字符串标签替换为数字标签
    # y = y.replace(to_replace=['no', 'yes'], value=[0, 1])
    # 分割数据为训练集和测试集
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    # 使用BP神经网络模型进行训练
    model = MLPClassifier(
        hidden_layer_sizes=(100, 50),
        max_iter=500, alpha=0.0001,
        solver='sgd',
        verbose=10,
        random_state=21,
        tol=0.000000001
    )

    model.fit(X_train, y_train)
    # 在这里保存模型
    joblib.dump(model, 'model.pkl')
    # 模型预测
    y_pred = model.predict(X_test)

    # 计算预测准确度
    accuracy = accuracy_score(y_true=y_test, y_pred=y_pred)
    print("Accuracy of the BP neural network model: ", accuracy)
    return jsonify({'data': y_pred.tolist()})

@app.route('/predict', methods=['POST'])
def predict():
    # 载入数据
    url = "E:/CSV/test_set.csv"
    data = pd.read_csv(url)
    X = data[['age', 'balance', 'day', 'duration', 'campaign', 'pdays']]
    y = data['y']
    scaler = StandardScaler()
    X = scaler.fit_transform(X)

    # 将“Y”中的字符串标签替换为数字标签
    # y = y.replace(to_replace=['no', 'yes'], value=[0, 1])
    # 分割数据为训练集和测试集
    X_test = X

    model = joblib.load('model.pkl')

    # 预测结果
    y_pred = model.predict(X_test)

    # 获取预测概率
    y_pred_proba = model.predict_proba(X_test)

    # 只保留预测结果为1的数据
    predicted_indices = np.where(y_pred == 1)

    # 预测结果为1的数据
    predicted_data = data.iloc[predicted_indices]

    # 预测结果为1的数据的预测概率
    predicted_proba = y_pred_proba[predicted_indices]

    # # 将预测概率转化为DataFrame
    # prob_df = pd.DataFrame(predicted_proba, columns=['probability'], index=predicted_data.index)
    #
    # # 将预测结果为1的数据和对应的预测概率放在一起
    # final_df = pd.concat([predicted_data, prob_df], axis=1)

    print("result")
    # print(predicted_data)
    # print(predicted_proba)
    json1 = predicted_data.to_json(orient="records")
    json2 = json.dumps(predicted_proba.tolist())
    print(json1)
    print(json2)
    # for(int i;i<)
    # print(final_df)
    # # 计算预测准确度
    # accuracy = accuracy_score(y_true=y_test, y_pred=y_pred)
    # print("Accuracy of the BP neural network model: ", accuracy)
    return jsonify({'data':predicted_data.to_json(orient="records"),
                    'proba':json.dumps(predicted_proba.tolist())})


class BPNN(nn.Module):
    def __init__(self, input_size):
        super(BPNN, self).__init__()
        self.fc1 = nn.Linear(input_size, 16)
        self.fc2 = nn.Linear(16, 1)

    def forward(self, x):
        x = torch.relu(self.fc1(x))
        x = self.fc2(x)
        return x


def bpmodle(df):
    # 独热编码
    # df = pd.get_dummies(data)

    # # 创建一个LabelEncoder对象
    # le = LabelEncoder()
    #
    # # 假设第一列是需要转换的列
    # df.iloc[:, 2] = le.fit_transform(df.iloc[:, 2])
    # df.iloc[:, 3] = le.fit_transform(df.iloc[:, 3])
    # df.iloc[:, 4] = le.fit_transform(df.iloc[:, 4])
    # df.iloc[:, 5] = le.fit_transform(df.iloc[:, 5])
    # df.iloc[:, 7] = le.fit_transform(df.iloc[:, 7])
    # df.iloc[:, 8] = le.fit_transform(df.iloc[:, 8])
    # df.iloc[:, 9] = le.fit_transform(df.iloc[:, 9])
    # df.iloc[:, 11] = le.fit_transform(df.iloc[:, 11])
    # df.iloc[:, 16] = le.fit_transform(df.iloc[:, 16])

    # 划分特征和目标变量
    # Y = df.iloc[:, -1]
    # X = df.iloc[:, :-1]
    X = df[['age', 'balance', 'day', 'duration', 'campaign', 'pdays']]
    Y = df['y']
    print(df.columns)

    scaler = StandardScaler()
    X = scaler.fit_transform(X)

    # 划分训练集和测试集
    X_train, X_test, y_train, y_test = train_test_split(X, Y, test_size=0.2, random_state=42)

    X_train = torch.tensor(X_train.astype(np.float32))
    print(1)
    y_train = torch.tensor(y_train.values.astype(np.float32))
    print(2)
    X_test = torch.tensor(X_test.astype(np.float32))
    print(3)
    y_test = torch.tensor(y_test.values.astype(np.float32))
    print(X_train)
    print(y_test)
    print("==========================================================")

    model = BPNN(X_train.shape[1])
    print(X_train.shape[1])
    # model = BPNN(X_train.shape[1], 8, 1)
    criterion = nn.MSELoss()
    optimizer = torch.optim.Adam(model.parameters(), lr=0.0001)

    # 训练模型
    for epoch in range(100):
        print(epoch)
        # model.zero_grad()
        # 前向传播
        outputs = model(X_train)
        loss = criterion(outputs, y_train)
        # 反向传播和优化
        optimizer.zero_grad()
        loss.backward()
        optimizer.step()

    # 利用测试集进行预测
    y_pred = model(X_test)

    # 评估模型性能，打印预测结果
    criterion = nn.MSELoss()
    loss = criterion(y_pred.squeeze(), y_test)
    print('Loss on test set: ', loss.item())

    return y_pred

@app.route('/model', methods=['POST'])
def model_predict():
    print('model')
    url = "E:/Desktop/csv/train_set.csv"
    data = pd.read_csv(url)
    arr2 = bpmodle(data)
    print(arr2)

@app.route('/user_job', methods=['POST'])
def get_job():
    url = "E:/CSV/train_set.csv"
    data = pd.read_csv(url)

    # 将分类数据转化为数值型
    data['job'] = data['job'].astype('category').cat.codes
    data['marital'] = data['marital'].astype('category').cat.codes
    data['education'] = data['education'].astype('category').cat.codes
    data['default'] = data['default'].map({'no': 0, 'yes': 1})
    data['loan'] = data['loan'].map({'no': 0, 'yes': 1})

    # 计算相关性
    correlation = data[['job', 'marital', 'education', 'default', 'balance', 'loan']].corr()

    return jsonify(correlation.to_dict())


if __name__ == "__main__":
    app.run(host='localhost', port=5000)