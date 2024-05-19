import numpy as np
import pandas as pd
import torch
import torch.nn as nn
import matplotlib.pyplot as plt
import os
os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"

# 读取数据
df = pd.read_csv("1880482.csv", index_col=0)  # 从 CSV 文件读取数据
arr = df.values.astype(np.float32)  # 将数据转换为 NumPy 数组并转换为 float32 类型
ts = torch.tensor(arr)  # 将数据转换为 PyTorch
# print(ts)
# 划分训练集和测试集
train_size = int(len(ts)*0.95)  # 计算训练集的大小
# ts = ts[torch.randperm(ts.size(0)), :]  # 随机打乱数据集


train_data = ts[:train_size]  # 划分训练集
test_data = ts[train_size:]  # 划分测试集

# 定义神经网络模型
class BPNN(nn.Module):
    def __init__(self):
        super(BPNN, self).__init__()
        self.fc1 = nn.Linear(6, 32)  # 输入层到隐藏层的全连接层，输入维度为 20，输出维度为 80
        self.fc2 = nn.Linear(32, 1)  # 隐藏层到输出层的全连接层，输入维度为 80，输出维度为 360

    def forward(self, x):
        x = torch.relu(self.fc1(x))  # 使用 ReLU 激活函数作用在隐藏层上
        x = self.fc2(x)  # 输出层，不使用激活函数
        return x

# 初始化模型并移动到 GPU 上
model = BPNN()

# 定义损失函数和优化器
criterion = nn.HuberLoss()  # 使用均方误差损失函数
optimizer = torch.optim.Adam(model.parameters(), lr=0.00001)  # 使用 Adam 优化器进行优化，学习率

# 准备数据

X_train = train_data[:, :6]  # 训练集特征
Y_train = train_data[:,-1]  # 训练集标签
X_test = test_data[:,:6]  # 测试集特征
Y_test = test_data[:,-1]  # 测试集标签
# print(X_test)
# print(Y_test)
# print("==========================================================")
# 训练模型
epochs = 2000  # 迭代次数
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
# model.eval()  # 设置模型为评估模式
# with torch.no_grad():  # 禁用梯度计算
#     outputs = model(X_test)  # 前向传播，计算输出
#     test_loss = criterion(outputs, Y_test)
#     test_losses.append(test_loss.item())  # 记录测试损失

# # 可视化
arr1 = outputs.cpu().numpy()
arr2 = Y_test.cpu().numpy()
print(arr1)

# def evaluate(actual, predicted):
#     # 检查数组长度是否相等且非零
#     if len(actual) != len(predicted) or len(actual) == 0:
#         raise ValueError("数组长度必须相等且非零")
#
#     # 计算实际值的平均值
#     mean_actual = np.mean(actual)
#
#     # 计算残差平方和（RSS）和 RMSE
#     rss = np.sum((actual - predicted) ** 2)
#     mse = rss / len(actual)
#     rmse = np.sqrt(mse)
#
#     # 计算总平方和（TSS）
#     tss = np.sum((actual - mean_actual) ** 2)
#
#     # 计算 R²
#     r2 = 1 - (rss / tss)
#
#     # 计算 AvgAcc
#     avg_acc = (1 - np.sum(np.abs(actual - predicted)) / np.sum(np.abs(actual))) * 100
#
#     # 计算 MinAcc
#     min_acc = (1 - np.max(np.abs(actual - predicted)) / np.mean(np.abs(actual))) * 100
#
#     # 计算最大误差
#     max_error = np.max(np.abs(actual - predicted))
#
#     # 创建字典保存结果
#     result = {
#         "R²": round(r2, 5),
#         "RMSE": round(rmse, 5),
#         "AvgAcc": "{:.3f}%".format(avg_acc),
#         "MinAcc": "{:.3f}%".format(min_acc),
#         "MaxError": round(max_error, 2)
#     }
#     return result
#
#
# # 计算评价指标
# evaluation_results = evaluate(arr2, arr1)
# print(evaluation_results)


# for t in range(len(arr1)):
#     Fig = plt.figure()
#     plt.plot(range(1, 361), arr1[t], label='predict')
#     plt.plot(range(1, 361), arr2[t], label='actual')
#     plt.xlabel('x')
#     plt.ylabel('strain')
#     plt.legend()
#     plt.show()
#
#
# # 可视化训练和测试损失
# plt.plot(train_losses, label='Train Loss')
# plt.plot(test_losses, label='Test Loss')
# plt.xlabel('Epochs')
# plt.ylabel('Loss')
# plt.legend()
# plt.show()
