package no.novari.fintkontrollorgunitservice

import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource
import no.novari.cache.FintCache
import no.novari.cache.FintCacheManager
import no.novari.fintkontrollorgunitservice.orgunit.OrgUnit
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.Locale

@Configuration
class CacheConfiguration(
    private val fintCacheManager: FintCacheManager,
) {
    @Bean
    fun organisasjonselementCache(): FintCache<String, OrganisasjonselementResource> {
        return createCache(OrganisasjonselementResource::class.java)
    }

    @Bean
    fun publishedOrgunitCache(): FintCache<String, OrgUnit> {
        return createCache(OrgUnit::class.java)
    }

    fun <V : Any> createCache(resourceClass: Class<V>) =
        fintCacheManager.createCache(
            resourceClass.name.lowercase(Locale.ROOT),
            String::class.java,
            resourceClass,
        )
}
