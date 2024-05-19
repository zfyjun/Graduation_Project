import numpy as np
import pandas as pd
import torch
import torch.nn as nn
import matplotlib.pyplot as plt
import json
from flask import Flask, request, jsonify

import os

from flask_cors import CORS

os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"

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






app = Flask(__name__)
CORS(app) # 允许跨域请求
@app.route("/train", methods=["POST"])
def train():
    type = request.json.get("type")
    data = request.json.get("data")
    if type==0:#bp神经网络
        preds=bpmodle(data)


    return jsonify({"code":200,'data':preds})

if __name__ == '__main__':
    app.run(host="127.0.0.1", port=5000)