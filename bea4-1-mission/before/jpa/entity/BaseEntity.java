package com.back.jpa.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass // JPA Entity 들이 BaseEntity 를 상속할 경우, BaseEntity 의 필드들도 컬럼으로 인식하게 함
@EntityListeners(AuditingEntityListener.class)
@Getter
// 모든 엔티티들의 조상
public class BaseEntity {
    public String getModelTypeCode() {
        return this.getClass().getSimpleName();
    }
}