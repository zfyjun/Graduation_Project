import numpy as np
import pandas as pd
import torch
import torch.nn as nn
import matplotlib.pyplot as plt
import json
from flask import Flask, request, jsonify

import os

from flask_cors import CORS
from sklearn.neural_network import MLPClassifier

os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"
#循环神经网络
def xunmodle(df):
    df=pd.json_normalize(df)#读取数据
    #数据拆分
    train_size = int(len(df)*0.9)
    data=df[:train_size] #读取训练集数据
    data2=df[train_size:] #读取测试集数据
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
    from sklearn.model_selection import train_test_split
    X_train, X_val, y_train, y_val = train_test_split(X, y, test_size=0.2,random_state=35)
    from sklearn.preprocessing import StandardScaler
    scaler = StandardScaler()
    scaler.fit(X_train)
    X_train_s = scaler.transform(X_train)
    X_val_s = scaler.transform(X_val)
    test_s = scaler.transform(test)
    from sklearn.ensemble import AdaBoostClassifier
    dfs = pd.DataFrame(columns=['date','adjustedclose'])
    dfs['date']=ID
    from sklearn.neural_network import MLPClassifier
# 神经网络
    model = MLPClassifier(hidden_layer_sizes=(16, 8), random_state=77, max_iter=10000)
    model.fit(X_train, y_train.astype('str'))

    pred = model.predict(test_s)
    dfs['adjustedclose']=pred
    return json.loads((dfs.to_json(orient='split',force_ascii=False)))

# 卷积神经网络
def juanmodle(df):
   # 读取数据
   df=pd.json_normalize(df)
   df=df[['date','open','high','low','close','volume','adjustedclose']]
   arr = df.values.astype(np.float32)
   ts = torch.tensor(arr)
   # 划分训练集和测试集
   train_size = int(len(ts) * 0.9)
   # ts = ts[torch.randperm(ts.size(0)), :]
   train_data = ts[:train_size]
   test_data = ts[train_size:]
   # 重新塑形数据为 CNN 输入
   X_train = train_data[:, :6].reshape(train_data.shape[0], 1, -1)
   # print(X_train.shape)
   Y_train = train_data[:,-1]  # 训练集标签
   # print(test_data.shape[0])
   X_test = test_data[:, :6].reshape(test_data.shape[0], 1, -1)
   Y_test = test_data[:,-1]  # 测试集标签
   # 初始化 CNN 模型
   cnn_model = CNN()

# 定义损失函数和优化器
   criterion_cnn = nn.HuberLoss()
   optimizer_cnn = torch.optim.Adam(cnn_model.parameters(), lr=0.0001)

# 训练 CNN 模型
   epochs = 2000
   for epoch in range(epochs):
      cnn_model.train()
      optimizer_cnn.zero_grad()
      outputs_cnn = cnn_model(X_train.clone().detach())
      loss_cnn = criterion_cnn(outputs_cnn, Y_train.clone().detach())
      loss_cnn.backward()
      optimizer_cnn.step()


   cnn_model.eval()
   with torch.no_grad():
      outputs_cnn_test = cnn_model(X_test.clone().detach())

   return outputs_cnn_test.tolist()
# bp神经网络
def bpmodle(df):
    # 读取数据
    df=pd.json_normalize(df)
    df=df[['date','open','high','low','close','volume','adjustedclose']]
    # df['date']=pd.to_datetime(df['date'])
    arr = df.values.astype(np.float32)  # 将数据转换为 NumPy 数组并转换为 float32 类型
    ts = torch.tensor(arr)  # 将数据转换为 PyTorch
# print(ts)
# 划分训练集和测试集
    train_size = int(len(ts)*0.9)  # 计算训练集的大小
# ts = ts[torch.randperm(ts.size(0)), :]  # 随机打乱数据集
    train_data = ts[:train_size]  # 划分训练集
    test_data = ts[train_size:]  # 划分测试集
# 初始化模型并移动到 GPU 上
    model = BPNN()
# 定义损失函数和优化器
    criterion = nn.HuberLoss()  # 使用均方误差损失函数
    optimizer = torch.optim.Adam(model.parameters(), lr=0.0001)  # 使用 Adam 优化器进行优化，学习率
# 准备数据

    X_train = train_data[:, :6]  # 训练集特征
    Y_train = train_data[:,-1]  # 训练集标签
    X_test = test_data[:,:6]  # 测试集特征
    Y_test = test_data[:,-1]  # 测试集标签
# print(X_test)
# print(Y_test)
# print("==========================================================")
# 训练模型
    epochs = 1500  # 迭代次数
    train_losses, test_losses = [], []  # 用于记录训练和测试损失的列表
    for epoch in range(epochs):
       model.train()  # 设置模型为训练模式
       optimizer.zero_grad()  # 梯度清零
       outputs = model(X_train)  # 前向传播，计算输出
       loss = criterion(outputs, Y_train)  # 计算损失
       loss.backward()  # 反向传播，计算梯度
       optimizer.step()  # 更新参数
       train_losses.append(loss.item())

# torch.save(model, 'E:\pycharmProject\py_projext\modelbp2.pth')
# print(X_test)

# 测试模型
    model.eval()  # 设置模型为评估模式
    with torch.no_grad():  # 禁用梯度计算
        outputs = model(X_test)  # 前向传播，计算输出
        test_loss = criterion(outputs, Y_test)
        test_losses.append(test_loss.item())  # 记录测试损失
    arr1 = outputs.cpu().numpy()

    return arr1.tolist()


# 定义bp神经网络模型
class BPNN(nn.Module):
    def __init__(self):
        super(BPNN, self).__init__()
        self.fc1 = nn.Linear(6, 80)  # 输入层到隐藏层的全连接层，输入维度为 20，输出维度为 80
        self.fc2 = nn.Linear(80, 1)  # 隐藏层到输出层的全连接层，输入维度为 80，输出维度为 360

    def forward(self, x):
        x = torch.relu(self.fc1(x))  # 使用 ReLU 激活函数作用在隐藏层上
        x = self.fc2(x)  # 输出层，不使用激活函数
        x=x.squeeze(-1)
        return x

# print(X_test)
# 定义 CNN 模型
class CNN(nn.Module):
    def __init__(self):
        super(CNN, self).__init__()
        self.conv1 = nn.Conv1d(in_channels=1, out_channels=6, kernel_size=3,padding=2)  # 第一个卷积层
        self.conv2 = nn.Conv1d(in_channels=6, out_channels=12, kernel_size=3,padding=2)  # 第二个卷积层
        self.avg_pool = nn.AvgPool1d(kernel_size=2)  # 平均汇聚层
        self.relu = nn.ReLU()
        self.fc1 = nn.Linear(60, 32)  # 第一个全连接层
        self.fc2 = nn.Linear(32, 1)  # 第二个全连接层

    def forward(self, x):
        x = self.conv1(x)
        x = self.relu(x)
        # print(x.shape)
        x = self.conv2(x)
        x = self.relu(x)
        # print(x.shape)
        x = self.avg_pool(x)
        x = self.relu(x)
        # print(x.shape)
        x = x.view(x.size(0), -1)  # 展平以用于全连接层
        x = self.fc1(x)
        x = self.relu(x)
        x = self.fc2(x)
        x=torch.squeeze(x,1)
        return x



app = Flask(__name__)
CORS(app) # 允许跨域请求
@app.route("/train", methods=["POST"])
def train():
    type = request.json.get("type")
    data = request.json.get("data")
    if type==0:#bp神经网络
        print("bp")
        preds=bpmodle(data)
    elif type==1:#卷积神经网络
        print("卷")
        preds=juanmodle(data)
    elif type==2:#循环神经网络
        print("循")
        preds=xunmodle(data)

    print(preds)
    return jsonify({"code":200,'data':preds})

if __name__ == '__main__':
    app.run(host="127.0.0.1", port=5000)