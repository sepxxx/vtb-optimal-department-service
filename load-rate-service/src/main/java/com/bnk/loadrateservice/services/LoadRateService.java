package com.bnk.loadrateservice.services;


import com.bnk.loadrateservice.dtos.Ids;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.log;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class LoadRateService {
//    ValueOperations<String, Long> valueOperations;
    RedisTemplate<String, String> redisTemplate;
    public static final String keyFormat = "%s:%s:%s";


    //возвращаем оценку от 0 до 10 по свободности
    //чем больше maxCurrentNum выше оценка
    //чем больше total ниже оценка
    public Double getLoadRateByOfficeId(Long id, String faceType) {
        String totalKey = keyFormat.formatted(String.valueOf(id),faceType,"total");
        String maxCurrentNumKey = keyFormat.formatted(String.valueOf(id),faceType,"current");
        LoadRateService.log.info("getLoadRateByOfficeId(), totalKey:{}, maxCurNumKey:{}",totalKey,maxCurrentNumKey);



        String totalString = redisTemplate.opsForValue().get(totalKey);
        String maxCurrentNumString = redisTemplate.opsForValue().get(maxCurrentNumKey);
        LoadRateService.log.info("getLoadRateByOfficeId(), string total:{}, string maxCurNum:{}",totalString,maxCurrentNumString);
        if(totalString!=null && maxCurrentNumString!=null) {
            Double total = Double.valueOf(totalString);
            Double maxCurrentNum = Double.valueOf(maxCurrentNumString);
            LoadRateService.log.info("getLoadRateByOfficeId(), total:{}, maxCurNum:{}",total,maxCurrentNum);

//            return maxCurrentNum/total*100;
            //казалось бы, просто чем больше maxCurrentNum, тем лучше
            //но вот примеры 8/10 = 80 , 800/1000 =80
            //в одном случае осталось 2 человека, а в другом 200

//            Формула включает логарифмическую функцию log(maxCurrentNum),
//            что увеличивает значимость разницы между значениями maxCurrentNum.
//            Таким образом, при более высоких значениях maxCurrentNum будет получаться более низкая оценка свободы,
//            так как разница в числе людей будет ощущаться более значимо.
            if(total==0) return 100D;
            if(maxCurrentNum.equals(total)) return 100D;
            return (maxCurrentNum / total) * 100 / (1 + log((total-maxCurrentNum)/30 + 1));

        }
        return -1D;
    }

    public Map<Long, Double> getLoadRatesByOfficesIds(List<Long> idsList , String faceType) {
        Map<Long, Double> loadRates = new HashMap<>();
        idsList.forEach((id)->{
            loadRates.put(
                    id, getLoadRateByOfficeId(id, faceType)
            );
        });
        return loadRates;
    }
}
