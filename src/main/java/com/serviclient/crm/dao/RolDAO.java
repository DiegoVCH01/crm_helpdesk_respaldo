package com.serviclient.crm.dao;

import com.serviclient.crm.entity.RolEntity;

import java.util.List;

public interface RolDAO {

    List<RolEntity> findAll();

    RolEntity save(RolEntity rol);
}