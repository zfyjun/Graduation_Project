import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import torch
import torch.nn as nn

data=pd.read_csv("1880482.csv")

data=data[['date','v']]
data

device ='cuda:0' if torch.cuda.is_available() else 'cpu'

data['date']=pd.to_datetime(data['date'])
# plt.plot(data['date'],data['v'])
# plt.show()

from copy import deepcopy as dc

def prepare_dataframe_for_lstm(df,n_steps):
    df=dc(df)
    df.set_index('date',inplace=True)
    for i in range(1,n_steps+1):
        df[f'v(t-{i})']=df['v'].shift(i)
    df.dropna(inplace=True)
    return df

lookback=7
shifted_df=prepare_dataframe_for_lstm(data,lookback)

shifted_df_as_np =shifted_df.to_numpy()

from sklearn.preprocessing import MinMaxScaler
scaler=MinMaxScaler(feature_range=(-1,1))
shifted_df_as_np=scaler.fit_transform(shifted_df_as_np)

X=shifted_df_as_np[:,1:]
y=shifted_df_as_np[:,0]
split_index=int(len(X)*0.95)

X_train=X[:split_index]
X_test=X[split_index:]
y_train=y[:split_index]
y_test=y[split_index:]

X_train=torch.tensor(X_train).float()
Y_train=torch.tensor(y_train).float()
X_test=torch.tensor(X_test).float()
Y_test=torch.tensor(y_test).float()

from torch.utils.data import Dataset
class TimeSeriesDataset(Dataset):
    def __int__(self,X,y):
        self.X=X
        self.y=y

    def __len__(self):
        return len(self.X)

    def __getitem__(self, i):
        return self.X[i],self.y[i]

train_dataset = TimeSeriesDataset(X_train,y_train)
test_dataset=TimeSeriesDataset(X_test,y_test)

from torch.utils.data import DataLoader
batch_size=16
train_loader=DataLoader(train_dataset,batch_size=batch_size,shuffle=True)
test_loader=DataLoader(test_dataset,batch_size=batch_size,shuffle=False)

for _,batch in enumerate(train_loader):
    X_batch,y_batch=batch[0].to(device),batch[1].to(device)
    print(X_batch.shape,y_batch.shape)
    break

class LSTM(nn.Module):
    def __int__(self,input_size,hidden_size,num_stacked_layers):
        super().__init__()
        self.hidden_size=hidden_size
        self.num_stacked_layers=num_stacked_layers
        self.lstm=nn.LSTM(input_size,hidden_size,num_stacked_layers,batch_first=True)
        self.fc=nn.Linear(hidden_size,1)

    def fprward(self,x):
        batch_size=x.size(0)
        h0=torch.zeros(self.num_stacked_layers,batch_size,self.hidden_size).to(device)
        c0=torch.zeros(self.num_stacked_layers,batch_size,self.hidden_size).to(device)
        out,_=self.lstm(x,(h0,c0))
        out=self.fc(out[:,-1:])
        return out

model=LSTM(1,4,1)
model.to(device)
model

learning_rate=0.001
num_epochs=10
loss_function=nn.MSELoss()
optimizer=torch.optim.Ada(model.parameters(),lr=learning_rate)

def train_one_epoch():
    model.train(True)
    print(f'Epoch:{epoch+1}')
    running_loss=0.0
    for batch_index,batch in enumerate(train_loader):
        x_batch,y_batch=batch[0].to(device),batch[1].to(device)
        output=model(x_batch)
        loss=loss_function(output,y_batch)
        running_loss+=loss
        optimizer.zero_grad()
        loss.backward()
        optimizer.step()
        if batch_index%100==99:
            avg_loss_across_batches=running_loss/100
            print('Batch{0},Loss:{1:.3f}'.format(batch_index+1,avg_loss_across_batches))
            running_loss=0.0
    print()

def validate_one_epoch():
    model.train(False)
    running_loss=0.0
    for batch_index,batch in enumerate(train_loader):
        x_batch,y_batch=batch[0].to(device),batch[1].to(device)
        with torch.no_grad():
           output = model(x_batch)
           loss=loss_function(output,y_batch)
           running_loss+=loss

    avg_loss_across_batches=running_loss/len(test_loader)
    print('val Loss:{0:.3f'.format(avg_loss_across_batches))
    print("***************************************************************")
    print()

learning_rate=0.001
num_epochs=10
loss_function=nn.MSELoss()
optimizer=torch.optim.Adam(model.parameters(),lr=learning_rate)

for epoch in range(num_epochs):
    train_one_epoch()
    validate_one_epoch()


with torch.no_grad():
    predicted=model(X_train.to(device)).to('cpu').numpy()

print(predicted)