import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense

# 加载数据
data_url = 'E:/CSV/train_set.csv'
data = pd.read_csv(data_url)

# 数据预处理，转化为数值型
le = LabelEncoder()
categorical_features = ["job", "marital", "education", "default", "housing", "loan", "contact", "month", "poutcome"]
for feature in categorical_features:
    data[feature] = le.fit_transform(data[feature])

# 分离特征和目标变量
features = data.drop(columns=["ID", "y"])
target = data["y"]

# 划分训练和测试数据集
X_train, X_test, y_train, y_test = train_test_split(features, target, test_size=0.2)

# 构建神经网络模型
model = Sequential()
model.add(Dense(32, input_dim=X_train.shape[1], activation='relu'))
model.add(Dense(16, activation='relu'))
model.add(Dense(1, activation='sigmoid'))

# 编译模型
model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['accuracy'])

# 训练模型
model.fit(X_train, y_train, epochs=10, batch_size=10)

# 模型评估
_, accuracy = model.evaluate(X_test, y_test)
print('Accuracy: %.2f' % (accuracy*100))