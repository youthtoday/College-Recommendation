import scrapy
from testhbase.items import TesthbaseItem

# 载入初始信息，注入start_url种子库
import pandas as pd
df = pd.read_excel(r"E:\python\useful_school.xlsx", usecols=[1,2,3,4,5,6,10],names=None)
df_li = df.values.tolist()  #0名称，1代码，2主管部门，3地址，4办学层次，5学校类别，6网页
sc_count=len(df_li)
# sc_count=1240
sc_id=[]
sc_name=[]
sc_url=[]
sc_belong=[]
sc_location = []
sc_level = []
sc_type = []
for i in range(sc_count):
    sc_name.append(df_li[i][0])
    sc_id.append(df_li[i][1])
    sc_belong.append(df_li[i][2])
    sc_location.append(df_li[i][3])
    sc_level.append(df_li[i][4])
    sc_type.append(df_li[i][5])
    sc_url.append(df_li[i][6])

#   判断是否包含中文字符
def is_contain_chinese(check_str):
    for ch in check_str:
        if u'\u4e00' <= ch <= u'\u9fff':
            return True
    return False

class TestSchoolSpider(scrapy.Spider):
    name = 'habase'
    start_urls = sc_url

    def __init__(self):
        self.count = 0
#   request请求
    def start_requests(self):
        # for url in self.start_urls:
        for i in range(sc_count):
            url=self.start_urls[i]
            yield scrapy.Request(url=url, callback=self.parse)
#   对response解析并返回数据
    def parse(self, response):
        for tag in range(sc_count):
            if response.url==sc_url[tag]:
                self.count=tag
        school_id = sc_id[self.count]
        school_name = sc_name[self.count]
        school_url = sc_url[self.count]
        school_belong = sc_belong[self.count]
        school_location = sc_location[self.count]
        school_level = sc_level[self.count]
        school_type = sc_type[self.count]
        school_html=response.text
        print('当前爬取学校：',school_id,school_name)
        # print('当前爬取学校：',self.count,school_id,school_name)
        base_info = response.xpath('//a[@href]')
        for a in base_info:
            t_text = a.xpath('./text()').extract_first()
            t_url = a.xpath('./@href').extract_first()
            if t_text == None:
                continue
            elif (is_contain_chinese(t_text) == False)or(len(t_url)==0):
                continue
            if 'http' not in t_url:
                t_url=response.url+t_url
            infos = TesthbaseItem(school_id=school_id,
                                  school_name=school_name,
                                  school_url=school_url,
                                  school_html=school_html,
                                  school_belong=school_belong,
                                  school_location = school_location,
                                  school_level = school_level,
                                  school_type = school_type,
                                  t_text=t_text,
                                  t_url=t_url)
            yield infos