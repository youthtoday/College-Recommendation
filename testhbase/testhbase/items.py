# Define here the models for your scraped items
#
# See documentation in:
# https://docs.scrapy.org/en/latest/topics/items.html

import scrapy


class TesthbaseItem(scrapy.Item):
    # define the fields for your item here like:
    # name = scrapy.Field()
    school_id = scrapy.Field()  # 大学标识码
    school_name = scrapy.Field()    # 学校名称
    school_url = scrapy.Field()     #官网地址
    school_html=scrapy.Field()      # html
    school_belong=scrapy.Field()    #学校所属
    school_location = scrapy.Field()    #学校地址
    school_level = scrapy.Field()   #办学层次
    school_type = scrapy.Field()    #办学类型
    t_text = scrapy.Field()  # 链接文本
    t_url = scrapy.Field()  # 链接网址
