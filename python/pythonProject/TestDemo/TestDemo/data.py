import pandas as pd
from datetime import datetime

# 读取csv数据
df = pd.read_csv('E:/Desktop/csv/1000026.csv')

# 检查是否有缺失值
if df.isnull().sum().sum() > 0:
    print("有缺失值.")
    print(df.values)
    # 可以选择填充缺失值，填充方式有很多种，这里以填充0为例
    df.dropna(subset=['date', 'a'], inplace=True)
else:
    print("无缺失值")

# 定义一个转化日期的函数
def convert_date(date_string):
    try:
        # 解析日期并重新格式化为'YYYYMMDD'格式
        return datetime.strptime(date_string, '%Y/%m/%d').strftime('%Y%m%d')
    except ValueError:
        return date_string

# 把函数应用到'date'列
df['date'] = df['date'].apply(convert_date)


# 根据需求，可以删除不需要的列，这里略

# 将清洗过的数据保存为新的csv文件
df.to_csv('E:/Desktop/csv/cleaned_data.csv', index=False)
print("数据清洗完成，已保存为'cleaned_data.csv'")