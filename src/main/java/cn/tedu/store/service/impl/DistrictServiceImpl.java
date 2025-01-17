package cn.tedu.store.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.tedu.store.entity.District;
import cn.tedu.store.mapper.DistrictMapper;
import cn.tedu.store.service.IDistrictService;

/**
 * 处理省/市/区数据的业务层实现类
 */
@Service
public class DistrictServiceImpl implements IDistrictService {
	
	@Autowired
	private DistrictMapper districtMapper;

	@Override
	public District getByCode(String code) {
		return districtMapper.findByCode(code);
	}
	
	@Override
	public List<District> getByParent(String parent) {
		List<District> list = districtMapper.findByParent(parent);
		for (District item : list) {
			item.setId(null);
			item.setParent(null);
		}
		return list;
	}

}





