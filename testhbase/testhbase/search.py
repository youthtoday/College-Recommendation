#   -*- codeing = utf-8 -*-
#   @Time : 2022/4/7 15:06
#   @Author : Xiong
#   @File : search.py
#   @Software : PyCharm
import pandas as pd
import happybase
import random
df = pd.read_excel(r"E:\python\useful_school.xlsx", usecols=[1, 2, 3, 4, 5, 6, 10], names=None)
# df = pd.read_excel("useful_school.xlsx", usecols=[1, 2, 3, 4, 5, 6, 10], names=None)
df_li = df.values.tolist()  # 0名称，1代码，2主管部门，3地址，4办学层次，5学校类别，6网页
sc_count = len(df_li)
sc_id = []
sc_name = []
sc_url = []
sc_belong = []
sc_location = []
sc_level = []
sc_type = []
for i in range(sc_count):
    sc_name.append(df_li[i][0])
    sc_id.append(str(df_li[i][1]))
    sc_belong.append(df_li[i][2])
    sc_location.append(df_li[i][3])
    sc_level.append(df_li[i][4])
    sc_type.append(df_li[i][5])
    sc_url.append(df_li[i][6])

print('正在尝试连接到HBase数据库……')
connection = happybase.Connection(host="122.205.5.3", port=19217)
print('连接成功!')
tables_list = connection.tables()
print('数据库包含有下列的表：')
for i in range(len(tables_list)):
    tables_list[i] = tables_list[i].decode('utf-8')
    print(tables_list[i])
intable = input('请输入您希望操作或查询或创建的表名:')

if intable not in tables_list:
    families = {
        'base_info': dict(),
        'hrefs': dict(),
        'majors':dict(),
    }
    connection.create_table(intable, families)  # 创建
    print('表已创建成功，已连接')
    table = connection.table(intable)
else:
    table = connection.table(intable)  # 获取表
    print('表已存在，已成功连接到表%s!'%intable)
    # print(type(table))
# for i in range(sc_count):
#     table.put(str(sc_id[i]),
#             {'base_info:name': sc_name[i],
#             'base_info:url': sc_url[i],
#             'base_info:belong': sc_belong[i],
#             'base_info:location': sc_location[i],
#             'base_info:level': sc_level[i],
#             'base_info:type': sc_type[i]})

while True:
    print('\n您希望对该表执行什么操作？\n0:查询%s表整体信息；\n1:查询大学详细信息；\nquit：退出。\n'%intable)
    judge = input("请输入您的选择：")
    if judge == 'quit':
        break
    elif judge == '1':
        info = input('请输入您想要查询的大学：')
        if info in sc_name:
            tag = sc_name.index(info)
            row_key = sc_id[tag]
        else:
            print('请检查输入后重试！')
            continue
        row = table.row(row_key)
        x=[]
        print(row.get(b'base_info:name').decode().strip())      # 获得某个单元格的值
        for key, value in row.items():    # key value遍历该row
            print(key.decode().strip(), value.decode().strip())
            x.append(key.decode().strip())
        choose = True
        while choose == True:
            print('\n您是否希望进一步查询该学校相关信息？\nn：退出。\ny:进行细致查询。')
            judge1=input('请输入您的选择：')
            if judge1 == 'n':
                choose=False
            elif judge1 == 'y':
                info2=input('请输入你想查看的信息:')
                for a in x:
                    if info2 in a:
                        try:
                            print(a,row.get(a.encode()).decode().strip())
                        except AttributeError:
                            pass

    elif judge == '0':
        print('正在为您做全表扫描，请耐心等待……')
        keys = []
        for key, value in table.scan():  # 遍历table
            keys.append(key.decode())
        num = len(keys)
        print('扫描完成，查询到%s表中有%d所大学的数据！\n包含以下大学的信息(下列大学信息为随机打印输出)：' % (intable,num))
        for i in range(10):     # 随即打印几个大学信息
            a = random.randint(0, num)
            row_key = keys[a]
            row = table.row(row_key)
            sname=row.get(b'base_info:name').decode().strip()
            surl=row.get(b'base_info:url').decode().strip()
            print(i + 1, row_key,sname,surl)
    else:
        print("输入有误,请检查输入后重试！")
