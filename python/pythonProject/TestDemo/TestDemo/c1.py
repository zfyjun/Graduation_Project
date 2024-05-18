import numpy as np
import pandas as pd
import torch
import torch.nn as nn
import matplotlib.pyplot as plt
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score

# 读取数据
df = pd.read_csv("1880482.csv", index_col=0)
arr = df.values.astype(np.float32)
ts = torch.tensor(arr).T

# 划分训练集和测试集
train_size = int(len(ts) - 1)
# ts = ts[torch.randperm(ts.size(0)), :]
train_data = ts[:train_size]
test_data = ts[train_size:]


# 重新塑形数据为 CNN 输入
X_train = train_data[:, ::18].reshape(train_data.shape[0], 1, -1)
# print(X_train.shape)
Y_train = train_data
# print(test_data.shape[0])
X_test = test_data[:, ::18].reshape(test_data.shape[0], 1, -1)
Y_test = test_data
# print(X_test.clone().detach())
# print(64 * ((X_train.shape[-1] - 4) // 2))
# 定义 CNN 模型
class CNN(nn.Module):
    def __init__(self):
        super(CNN, self).__init__()
        self.conv1 = nn.Conv1d(in_channels=1, out_channels=32, kernel_size=3, padding=1)  # 第一个卷积层
        self.conv2 = nn.Conv1d(in_channels=32, out_channels=64, kernel_size=3)  # 第二个卷积层
        self.avg_pool = nn.AvgPool1d(kernel_size=2)  # 平均汇聚层
        self.relu = nn.ReLU()
        self.fc1 = nn.Linear(576, 256)  # 第一个全连接层
        self.fc2 = nn.Linear(256, 360)  # 第二个全连接层

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
        return x

# torch.Size([17, 1, 20])
# 1 torch.Size([17, 32, 18])
# 2 torch.Size([17, 64, 16])
# 3 torch.Size([17, 64, 8])
# 4 torch.Size([17, 512])
# 5 torch.Size([17, 256])
# 6 torch.Size([17, 360])

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

print(test_losses_cnn)

# 转换预测结果和真实标签为 NumPy 数组
arr1 = outputs_cnn_test.cpu().numpy()
arr2 = Y_test.cpu().numpy()

def evaluate(actual, predicted):
    # 检查数组长度是否相等且非零
    if len(actual[0]) != len(predicted[0]) or len(actual[0]) == 0:
        raise ValueError("数组长度必须相等且非零")

    # 计算实际值的平均值
    mean_actual = np.mean(actual)

    # 计算残差平方和（RSS）和 RMSE
    rss = np.sum((actual - predicted) ** 2)
    mse = rss / len(actual[0])
    rmse = np.sqrt(mse)

    # 计算总平方和（TSS）
    tss = np.sum((actual - mean_actual) ** 2)

    # 计算 R²
    r2 = 1 - (rss / tss)

    # 计算 AvgAcc
    avg_acc = (1 - np.sum(np.abs(actual - predicted)) / np.sum(np.abs(actual))) * 100

    # 计算 MinAcc
    min_acc = (1 - np.max(np.abs(actual - predicted)) / np.mean(np.abs(actual))) * 100

    # 计算最大误差
    max_error = np.max(np.abs(actual - predicted))

    # 创建字典保存结果
    result = {
        "R²": round(r2, 5),
        "RMSE": round(rmse, 5),
        "AvgAcc": "{:.3f}%".format(avg_acc),
        "MinAcc": "{:.3f}%".format(min_acc),
        "MaxError": round(max_error, 2)
    }
    return result


# 计算评价指标
evaluation_results = evaluate(arr2, arr1)
print(evaluation_results)

# 可视化预测结果
for t in range(len(arr1)):
    plt.figure()
    plt.plot(range(1, 361), arr1[t], label='predict')
    plt.plot(range(1, 361), arr2[t], label='actual')
    plt.xlabel('x')
    plt.ylabel('y')
    plt.legend()
    plt.show()

# 可视化训练损失
plt.plot(train_losses_cnn, label='Train_Loss')
plt.plot(test_losses_cnn, label='Test_Loss')
plt.xlabel('Epochs')
plt.ylabel('Loss')
plt.title('Loss line')
plt.legend()
plt.show()
