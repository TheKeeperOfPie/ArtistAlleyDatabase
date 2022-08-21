import com.thekeeperofpie.artistalleydatabase.json_schema.JsonSchemaExtension
import com.thekeeperofpie.artistalleydatabase.raml.RamlExtension

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("com.thekeeperofpie.artistalleydatabase.raml")
    kotlin("plugin.serialization") version "1.7.10"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

configure<JsonSchemaExtension> {
    urls.set(
        listOf(
            "https://vgmdb.info/schema/artist.json",
            "https://vgmdb.info/schema/album.json",
            "https://vgmdb.info/schema/artist.json",
            "https://vgmdb.info/schema/event.json",
            "https://vgmdb.info/schema/org.json",
            "https://vgmdb.info/schema/product.json",
            "https://vgmdb.info/schema/release.json",
            "https://vgmdb.info/schema/search.json",
            "https://vgmdb.info/schema/sellers.json",
            "https://vgmdb.info/schema/recent.json",
        )
    )
    urlsCustomNames.set(
        mapOf(
            "https://vgmdb.info/schema/albumlist.json" to "AlbumList",
            "https://vgmdb.info/schema/artistlist.json" to "ArtistList",
            "https://vgmdb.info/schema/eventlist.json" to "EventList",
            "https://vgmdb.info/schema/orglist.json" to "OrgList",
            "https://vgmdb.info/schema/productlist.json" to "ProductList",
        )
    )
    @Suppress("SpellCheckingInspection")
    customPropertyNames.set(
        mapOf(
            "deathdate" to "deathDate",
            "albumlist" to "albumList",
            "artistlist" to "artistList",
            "productlist" to "productList",
            "orglist" to "orgList",
            "eventlist" to "eventList",
        )
    )
}

configure<RamlExtension> {
    baseUrl.set("https://vgmdb.info")
}