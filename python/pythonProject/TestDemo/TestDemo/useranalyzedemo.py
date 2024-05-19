import json
import pandas as pd
import pymysql
from flask import Flask, jsonify, request
from flask_cors import CORS
from matplotlib import pyplot as plt
from sklearn.cluster import KMeans
from sqlalchemy import create_engine
import numpy as np


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
        # print(wcss)
    # plt.plot(range(1, 11), wcss)
    # plt.title('Elbow Method')
    # plt.xlabel('Number of clusters')
    # plt.ylabel('WCSS')
    # plt.show()

    # 根据Elboe Method的结果选择合适的簇数，然后用K-means算法进行分群
    n_clusters = 5  # 假设我们选择了5
    kmeans = KMeans(n_clusters=n_clusters, init='k-means++', max_iter=300, n_init=10, random_state=0)
    y_kmeans = kmeans.fit_predict(X)
    # print(y_kmeans)

    clusters = []
    for i in range(n_clusters):
        cluster_i = X[y_kmeans == i][['age', 'balance']].values.tolist()
        clusters.append(cluster_i)
    # centers = kmeans.cluster_centers_.tolist()
    # 以JSON格式返回数据
    print(jsonify({'clusters': clusters}))
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
    print(describe_age)


    # 计算工作类型的分布频数
    job_data = data['job'].value_counts().reset_index()
    job_data.columns = ['job', 'count']
    job_data_dict = job_data.to_dict(orient='records')

    tmp1 = data['job'].describe().to_dict()
    describe_job = json.dumps(tmp1, default=default)

    # describe_job = jsonify(data['job'].describe().to_dict())
    # describe_job1 = jsonify(data['job'].describe().to_dict())
    print("job")
    print(tmp1)
    print(describe_job)

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
    # return jsonify({'name': name,
    #                 'age': age_data_dict,
    #                 'job': job_data_dict,
    #                 'balance': balance_data_dict,
    #                 'marital': marital_data_dict,
    #                 'education': education_data_dict})

@app.route('/user_job', methods=['POST'])
def get_job():
    url = "E:/CSV/train_set.csv"
    data = pd.read_csv(url)


    Vuedata = request.get_json()
    print(Vuedata)
    dataname1 = Vuedata['dataname1']
    dataname2 = Vuedata.get('dataname2')
    print(dataname1)
    print(dataname2)

    # 进行描述性统计分析
    describe_data = data['age'].describe()
    # print(describe_data)

    # dataname1 = 'age'
    # dataname2 = 'balance'

    # 截取年龄和账户余额数据
    # age_balance_data = data[[dataname1, dataname2]].values.tolist()
    # print(age_balance_data)





    data['education_encoded'] = data['education'].astype('category').cat.codes
    # print(data['education_encoded'])
    data['loan_encoded'] = data['loan'].map({'no': 0, 'yes': 1})

    education_data = data.groupby('y')['education_encoded'].mean().to_dict()
    loan_data = data.groupby('y')['loan_encoded'].mean().to_dict()

    # 对 'job' 和 'marital' 进行分组，然后计算每组中 'age' 和 'balance' 的平均值
    result = data.groupby(['job', 'marital'])[['age', 'balance']].mean()
    print(result)
    # 将分组结果转换为 JSON 格式并打印
    json_result = result.to_json(orient='index')
    print('json_result')
    print(json_result)



    # 计算年龄（age）和账户余额（balance）的相关性
    # correlation = data[dataname1].corr(data[dataname2])
    # 'correlation': correlation
    return jsonify({'education': education_data,
                    'loan': loan_data,
                    'result': json_result})





if __name__ == "__main__":
    app.run(host='localhost', port=5000)