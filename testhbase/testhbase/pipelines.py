# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://docs.scrapy.org/en/latest/topics/item-pipeline.html


# useful for handling different item types with a single interface
from itemadapter import ItemAdapter
import happybase

class TesthbasePipeline:
    def open_spider(self, spider):
        print('爬虫程序启动，将为您爬取大学官网信息并存储到HBase数据库！')
        print('正在尝试连接到HBase数据库……')
        self.connection = happybase.Connection(host="122.205.5.3", port=19217)
        print('连接成功!')
        tables_list = self.connection.tables()
        print('数据库包含有下列的表：')
        for i in range(len(tables_list)):
            tables_list[i] = tables_list[i].decode('utf-8')
            print(tables_list[i])
        # intable = input('请输入您希望操作或查询或创建的表名:')
        intable = 'school'
        if intable not in tables_list:
            families = {
                'base_info': dict(),
                'herfs': dict(),    # 将错就错了
                'majors': dict(),
            }
            self.connection.create_table(intable, families)  # 创建
            print('表已创建成功，已连接')
            self.created_table = self.connection.table(intable)
        else:
            self.created_table = self.connection.table(intable)  # 获取表
            print('表已存在，已成功连接到表%s!' % intable)


    def process_item(self, item, spider):
        hrefurl= 'herfs:'+item['t_text'] #提取的信息
        self.created_table.put(str(item['school_id']),
                          {'base_info:name': item['school_name'],
                           'base_info:url': item['school_url'],
                           'base_info:html': item['school_html'],
                           'base_info:belong': item['school_belong'],
                           'base_info:location': item['school_location'],
                           'base_info:level': item['school_level'],
                           'base_info:type': item['school_type'],
                           hrefurl: item['t_url']})
        return item

    def close_spider(self, spider):
        print('爬虫程序运行结束！')
        self.connection.close()

