import pandas as pd
import pymysql
from flask import Flask, jsonify
from flask_cors import CORS
from matplotlib import pyplot as plt
from sklearn.cluster import KMeans
from sqlalchemy import create_engine

pymysql.install_as_MySQLdb()
engine = create_engine('mysql://root:root@localhost:3306/test?charset=utf8')


app = Flask(__name__)
CORS(app) # 允许跨域请求


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
    centers = kmeans.cluster_centers_.tolist()
    # 以JSON格式返回数据
    print(jsonify({'clusters': clusters, 'centers': centers}))
    return jsonify({'clusters': clusters, 'centers': centers})

if __name__ == "__main__":
    app.run(host='localhost', port=5000)