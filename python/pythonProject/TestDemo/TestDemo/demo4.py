import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.tree import DecisionTreeClassifier
from sklearn.metrics import accuracy_score

# 加载数据
data = pd.read_csv('E:/CSV/train_set.csv')

# 希望预测的目标变量的列名是 'y'
target = 'y'

# 将分类变量进行编码
for column in data.columns:
  if data[column].dtype == 'object':
    lbl = LabelEncoder()
    lbl.fit(list(data[column].values))
    data[column] = lbl.transform(list(data[column].values))

# 分离特征和目标变量
X = data.drop(target, axis=1)
y = data[target]

# 分割数据集为训练集和测试集
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# 初始化决策树模型
model = DecisionTreeClassifier()

# 训练模型
model.fit(X_train, y_train)

# 用模型预测测试集
y_pred = model.predict(X_test)

# 输出模型的准确率
print('Accuracy:', accuracy_score(y_test, y_pred))