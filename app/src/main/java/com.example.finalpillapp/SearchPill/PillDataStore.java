package com.example.finalpillapp.SearchPill;

import com.example.finalpillapp.PillInfo.PillInfo;

import java.util.ArrayList;
import java.util.List;

public class PillDataStore {

    // PillDataStore의 단일 인스턴스를 저장할 변수 (싱글톤 패턴 적용)
    private static PillDataStore instance;

    // Pill 객체를 저장할 리스트
    private List<PillInfo> pillList;

    // 생성자: pillList를 ArrayList로 초기화
    // 외부에서 객체 생성을 막기 위해 private으로 설정
    private PillDataStore() {
        pillList = new ArrayList<>();
    }

    // 싱글톤 패턴을 사용하여 PillDataStore의 인스턴스를 반환
    public static synchronized PillDataStore getInstance() {
        // 인스턴스가 없으면 새로 생성하여 반환
        if (instance == null) {
            instance = new PillDataStore();
        }
        return instance;
    }

    // pillList를 반환하는 메서드
    // 이 메서드를 통해 저장된 약물 리스트에 접근할 수 있음
    public List<PillInfo> getPillList() {
        return pillList;
    }

    // pillList를 설정하는 메서드
    // 새로운 약물 리스트를 설정하거나 업데이트할 때 사용
    public void setPillList(List<PillInfo> pillList) {
        this.pillList = pillList;
    }
}
