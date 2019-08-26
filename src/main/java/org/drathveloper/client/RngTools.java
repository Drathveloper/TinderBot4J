package org.drathveloper.client;

import java.util.*;

class RngTools {

    public int generateRandomDelay(int min, int max){
        Date today = new Date();
        Random random = new Random(today.getTime());
        return random.nextInt(max - min) + min;
    }

    public List<Boolean> generateWeightedRandomDistribution(int listSize, double likePercent){
        int trueNum = (int) Math.ceil(listSize*likePercent);
        int falseNum = listSize - trueNum;
        List<Boolean> flagList = new ArrayList<>();
        for(int i=0; i<trueNum; i++){
            flagList.add(true);
        }
        for(int i=0; i<falseNum; i++){
            flagList.add(false);
        }
        Collections.shuffle(flagList);
        return flagList;
    }
}
