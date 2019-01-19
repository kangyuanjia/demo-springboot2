package com.yinww.demo.springboot2.demo012.controller;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yinww.demo.springboot2.demo012.domain.City;
import com.yinww.demo.springboot2.demo012.domain.Province;
import com.yinww.demo.springboot2.demo012.service.SpiderService;
import com.yinww.demo.springboot2.demo012.util.HttpUtil;

@RestController
public class SpiderController {
    
    @Autowired
    private SpiderService spiderService;
    
    @GetMapping({"/", ""})
    public Object spider() throws Exception {
        String url = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/";
        String charset = "gb2312";
        Document rootDoc = HttpUtil.get(url, charset);
        if(rootDoc == null) {
            return 0;
        }
        Element firstElement = rootDoc.getElementsByClass("center_list_contlist").get(0);
        String yearHref = firstElement.select("a").get(0).attr("href"); // 最近一个年份的省份链接
        Document doc = HttpUtil.get(yearHref, charset);
        
        // 遍历所有的省
        Elements provinceElements = doc.getElementsByClass("provincetr");
        for (Element element : provinceElements) {
            Elements aEles = element.select("a");
            for (Element aEle : aEles) {
                String name = aEle.text();
                String provincesHref = aEle.attr("href");
                String code = provincesHref.substring(0, provincesHref.indexOf("."));
                int index = yearHref.lastIndexOf("/") + 1;
                provincesHref = yearHref.substring(0, index) + provincesHref;
                Province province = new Province(name, code);
                spiderService.saveProvince(province);
                getCites(provincesHref, charset, province.getId());
            }
        }
        
        return "spider crawl end.";
    }
    
    private void getCites(String url, String charset, int provinceId) throws Exception {
        Document rootDoc = HttpUtil.get(url, charset);
        if(rootDoc != null) {
            Elements cityElements = rootDoc.getElementsByClass("citytr");
            for (Element cityElement : cityElements) {
                Element aEle = cityElement.select("a").get(1); // 第二个是市的名字
                String name = aEle.text();
                String cityHref = aEle.attr("href");
                City city = new City();
                city.setName(name);
                city.setProvinceId(provinceId);
                int start = cityHref.lastIndexOf("/") + 1;
                String code = cityHref.substring(start, cityHref.indexOf("."));
                city.setCode(code);
                spiderService.saveCity(city);
            }
        }
    }

}
