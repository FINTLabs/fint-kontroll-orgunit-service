package no.novari.fintkontrollorgunitservice

import no.fint.model.resource.FintLinks
import no.fint.model.resource.Link
import no.novari.fintkontrollorgunitservice.organisasjonsenhet.NoSuchLinkException

class FintLinkUtils {
    companion object {
        @JvmStatic
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
