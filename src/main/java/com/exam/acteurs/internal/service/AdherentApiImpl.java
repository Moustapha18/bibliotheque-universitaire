package com.exam.acteurs.internal.service;

import com.exam.acteurs.api.AdherentApi;
import com.exam.acteurs.api.AdherentInfo;
import com.exam.acteurs.internal.mapper.AdherentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdherentApiImpl implements AdherentApi {

    private final AdherentService adherentService;
    private final AdherentMapper mapper;

    @Override
    public AdherentInfo getAdherent(Long adherentId) {
        return mapper.toInfo(adherentService.getOrThrow(adherentId));
    }
}
