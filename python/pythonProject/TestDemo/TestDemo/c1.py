import numpy as np
import pandas as pd
import torch
import torch.nn as nn
import matplotlib.pyplot as plt
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score

# 读取数据
df = pd.read_csv("1880482.csv", index_col=0)
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

# 初始化 CNN 模型
cnn_model = CNN()

# 定义损失函数和优化器
criterion_cnn = nn.HuberLoss()
optimizer_cnn = torch.optim.Adam(cnn_model.parameters(), lr=0.0001)

# 训练 CNN 模型
epochs = 2000
train_losses_cnn, test_losses_cnn = [], []
for epoch in range(epochs):
    cnn_model.train()
    optimizer_cnn.zero_grad()
    outputs_cnn = cnn_model(X_train.clone().detach())
    loss_cnn = criterion_cnn(outputs_cnn, Y_train.clone().detach())
    loss_cnn.backward()
    optimizer_cnn.step()
    train_losses_cnn.append(loss_cnn.item())

# torch.save(cnn_model, 'E:\pycharmProject\py_projext\cnn1.pth')

# 评估 CNN 模型
cnn_model.eval()
with torch.no_grad():
    outputs_cnn_test = cnn_model(X_test.clone().detach())
    test_loss_cnn = criterion_cnn(outputs_cnn_test, Y_test.clone().detach())
    test_losses_cnn.append(test_loss_cnn.item())

print(outputs_cnn_test)

