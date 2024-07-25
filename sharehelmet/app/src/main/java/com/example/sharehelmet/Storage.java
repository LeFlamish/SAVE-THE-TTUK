package com.example.sharehelmet;

import java.util.ArrayList;
import java.util.HashMap;

public class Storage {
    //그리고 이 스토리지들을 전부 한번에 담는 폴더가 있어야 함
    //처음에 HashMap<String, Object> Storage_list -> 키 값은 : ID, Storage 객체
    //그러면 보관소 리스트처럼 된 HashMap이 나올 거고 이걸 스토리지들을 전부 한번에 담는 폴더에 저장하면 됨

    //보관소의 ID
    String LocationID;

    //헬멧의 id 저장
    ArrayList<String> stored;
    HashMap<String, Object> findHelmet;
    //stored에서 헬멧의 id 를 가져오고, findHelmet의 키를 id 로 받아서 헬멧 객체를 가져온다


}
