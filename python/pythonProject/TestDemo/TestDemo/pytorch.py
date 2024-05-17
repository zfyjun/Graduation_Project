import torch
import torch.nn as nn
import matplotlib.pyplot as plt
# %matplotlib inline

from matplotlib_inline import backend_inline
backend_inline.set_matplotlib_formats('svg')
import pandas as pd
# 生成数据集
data = pd.read_csv('1880482.csv') #读取数据集合
# data = data.to('cuda:0')
data.shape

# 划分训练集和测试集
train_size=int(len(data)*0.7)
test_size=len(data)-train_size
data=data[torch.randperm( data.size(0)),:] #打乱样本
train_data=data[:train_size:] #训练集
test_data=data[train_size,:] #样本集
train_data.shape , test_data.shape

class DNN(nn.Module):

    def __int__(self):
        super(DNN,self).__init__()
        self.net = nn.Sequential(
            nn.Linear(3,5), nn.ReLU(),
            nn.Linear(5,5), nn.ReLU(),
            nn.Linear(5,5), nn.ReLU(),
            nn.Linear(5,3)
        )

    def forward(self,x):
        y =self.net(x) #x是输入数据，y是输出数据
        return y

model = DNN()
