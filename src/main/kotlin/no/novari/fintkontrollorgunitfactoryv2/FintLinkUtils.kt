package no.novari.fintkontrollorgunitfactoryv2

import no.fint.model.resource.FintLinks
import no.fint.model.resource.Link
import no.novari.fintkontrollorgunitfactoryv2.organisasjonsenhet.NoSuchLinkException

class FintLinkUtils {
    companion object {
        @JvmStatic
        fun getSystemIdFromPath(path: String): String {
            return path.substring(path.lastIndexOf('/') + 1)
        }

        fun getSystemIdFromMessageKey(path: String): String {
            return path.substringAfterLast('/')
        }

        @JvmStatic
        fun getFirstLink(
            linkProducer: java.util.function.Supplier<List<Link>>,
            resource: FintLinks,
            linkedResourceName: String,
        ): String {
            return linkProducer
                .get()
                .firstOrNull()
                ?.href
                ?.let(this::systemIdToLowerCase)
                ?: throw NoSuchLinkException.noLink(resource, linkedResourceName)
        }

        @JvmStatic
        fun systemIdToLowerCase(path: String) = path.replace("systemId", "systemid")

        @JvmStatic
        fun organisasjonsIdToLowerCase(path: String) = path.replace("organisasjonsId", "organisasjonsid")

        @JvmStatic
        fun getFirstSelfLink(resource: FintLinks): String =
            resource.selfLinks
                .firstOrNull()
                ?.href
                ?.let(this::systemIdToLowerCase)
                ?: throw NoSuchLinkException.noSelfLink(resource)
    }
}
