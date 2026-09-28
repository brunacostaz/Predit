package br.com.fiap.predit.risk.service;

import br.com.fiap.predit.risk.api.CampaignDtos.*;
import br.com.fiap.predit.risk.domain.Campaign;
import br.com.fiap.predit.risk.domain.CampaignRepository;
import br.com.fiap.predit.risk.error.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CampaignService {
    private static final Logger log = LoggerFactory.getLogger(CampaignService.class);
    private final CampaignRepository campaigns;

    public CampaignService(CampaignRepository campaigns) { this.campaigns = campaigns; }

    @Transactional(readOnly = true)
    public List<CampaignResponse> list() {
        return campaigns.findAllByOrderByCreatedAtDesc().stream().map(CampaignResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CampaignResponse find(UUID id) {
        return CampaignResponse.from(campaigns.findById(id).orElseThrow(() -> new NotFoundException("Campaign not found")));
    }

    @Transactional
    public CampaignResponse create(CreateCampaignRequest request) {
        Campaign campaign = campaigns.save(new Campaign(request.name(), request.description(), request.segment(),
                request.consentRequired(), request.eligibleCustomers(), request.estimatedConversionRate(),
                request.estimatedRevenue()));
        log.info("event=campaign_created campaignId={} actor={}", campaign.getId(), actor());
        return CampaignResponse.from(campaign);
    }

    @Transactional
    public CampaignResponse updateStatus(UUID id, UpdateCampaignStatusRequest request) {
        Campaign campaign = campaigns.findById(id).orElseThrow(() -> new NotFoundException("Campaign not found"));
        campaign.changeStatus(request.status());
        log.info("event=campaign_status_changed campaignId={} status={} actor={}", id, request.status(), actor());
        return CampaignResponse.from(campaign);
    }

    private String actor() { return SecurityContextHolder.getContext().getAuthentication().getName(); }
}
