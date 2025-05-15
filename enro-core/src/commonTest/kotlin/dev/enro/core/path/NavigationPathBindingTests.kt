package dev.enro.core.path

import dev.enro.core.NavigationKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NavigationPathBindingTests {
    @Test
    fun `getPath matching simple single segment path`() {
        val binding = NavigationPathBinding(
            keyType = NavigationKey::class,
            pattern = "test",
            deserialize = { TODO() },
            serialize = { TODO() },
        )
        // Simple cases with/without leading/trailing slashes
        assertTrue { binding.matches(ParsedPath.fromString("test")) }
        assertTrue { binding.matches(ParsedPath.fromString("test/")) }
        assertTrue { binding.matches(ParsedPath.fromString("/test")) }
        assertTrue { binding.matches(ParsedPath.fromString("/test/")) }

        // Query parameters are optional, so should not affect the match
        assertTrue { binding.matches(ParsedPath.fromString("/test/?query=123")) }
        assertTrue { binding.matches(ParsedPath.fromString("/test?query=123")) }
        assertTrue { binding.matches(ParsedPath.fromString("test?query=123")) }

        // Additional segments should not match
        assertFalse { binding.matches(ParsedPath.fromString("/test/123")) }

        // An empty initial segment should not match
        assertFalse { binding.matches(ParsedPath.fromString("//test")) }
    }

    @Test
    fun `getPath matching simple multi segment path`() {
        val binding = NavigationPathBinding(
            keyType = NavigationKey::class,
            pattern = "test/example/next",
            deserialize = { TODO() },
            serialize = { TODO() },
        )
        // Simple cases with/without leading/trailing slashes
        assertTrue { binding.matches(ParsedPath.fromString("test/example/next")) }
        assertTrue { binding.matches(ParsedPath.fromString("test/example/next/")) }
        assertTrue { binding.matches(ParsedPath.fromString("/test/example/next")) }
        assertTrue { binding.matches(ParsedPath.fromString("/test/example/next/")) }

        // Query parameters are optional, so should not affect the match
        assertTrue { binding.matches(ParsedPath.fromString("/test/example/next/?query=123")) }
        assertTrue { binding.matches(ParsedPath.fromString("/test/example/next?query=123")) }
        assertTrue { binding.matches(ParsedPath.fromString("test/example/next?query=123")) }

        // Additional segments should not match
        assertFalse { binding.matches(ParsedPath.fromString("/test/example/next/extra")) }
        assertFalse { binding.matches(ParsedPath.fromString("/test/extra/example/next")) }

        // Empty segments should not match
        assertFalse { binding.matches(ParsedPath.fromString("//test/example/next")) }
        assertFalse { binding.matches(ParsedPath.fromString("/test//example/next")) }
        assertFalse { binding.matches(ParsedPath.fromString("/test/example//next")) }
    }

    @Test
    fun `getPath matching multi segment path with placeholders`() {
        val binding = NavigationPathBinding(
            keyType = NavigationKey::class,
            pattern = "test/{id}/example/{name}",
            deserialize = { TODO() },
            serialize = { TODO() },
        )

        // Simple cases with/without leading/trailing slashes
        assertTrue { binding.matches(ParsedPath.fromString("test/123/example/john")) }
        assertTrue { binding.matches(ParsedPath.fromString("test/123/example/john/")) }
        assertTrue { binding.matches(ParsedPath.fromString("/test/123/example/john")) }
        assertTrue { binding.matches(ParsedPath.fromString("/test/123/example/john/")) }

        // Query parameters are optional, so should not affect the match
        assertTrue { binding.matches(ParsedPath.fromString("/test/123/example/john/?query=123")) }
        assertTrue { binding.matches(ParsedPath.fromString("/test/123/example/john?query=123")) }
        assertTrue { binding.matches(ParsedPath.fromString("test/123/example/john?query=123")) }

        // Additional segments should not match
        assertFalse { binding.matches(ParsedPath.fromString("/test/123/example/john/extra")) }
        assertFalse { binding.matches(ParsedPath.fromString("/test/extra/123/example/john")) }

        // Empty segments should not match
        assertFalse { binding.matches(ParsedPath.fromString("//test/123/example/john")) }
        assertFalse { binding.matches(ParsedPath.fromString("/test/123//example/john")) }
    }

    @Test
    fun `getPath matching multi segment path with optional parameters`() {
        val binding = NavigationPathBinding(
            keyType = NavigationKey::class,
            pattern = "test/{id}/example/{name}?required={required}&optional={optional?}",
            deserialize = { TODO() },
            serialize = { TODO() },
        )

        // Simple cases with/without leading/trailing slashes
        assertTrue { binding.matches(ParsedPath.fromString("test/123/example/john?required=123")) }
        assertTrue { binding.matches(ParsedPath.fromString("test/123/example/john/?required=123")) }
        assertTrue { binding.matches(ParsedPath.fromString("/test/123/example/john?required=123")) }
        assertTrue { binding.matches(ParsedPath.fromString("/test/123/example/john/?required=123")) }

        // Query parameters are optional, so should not affect the match
        assertTrue { binding.matches(ParsedPath.fromString("/test/123/example/john/?required=123&query=123")) }
        assertTrue { binding.matches(ParsedPath.fromString("/test/123/example/john?required=123&query=123")) }
        assertTrue { binding.matches(ParsedPath.fromString("test/123/example/john?required=123&query=123")) }

        // Missing required parameters should not match
        assertFalse { binding.matches(ParsedPath.fromString("/test/123/example/john/?query=123")) }
        assertFalse { binding.matches(ParsedPath.fromString("/test/123/example/john?query=123&optional=123")) }
        assertFalse { binding.matches(ParsedPath.fromString("/test/123/example/john?optional=456")) }
    }

    @Test
    fun `fromPath for root`() {
        val binding = NavigationPathBinding(
            keyType = NavigationKey::class,
            pattern = "/",
            deserialize = { ObjectKey },
            serialize = { TODO() },
        )

        assertEquals(ObjectKey, binding.fromPath(ParsedPath.fromString("/")))
        assertEquals(ObjectKey, binding.fromPath(ParsedPath.fromString("")))
    }

    @Test
    fun `fromPath for single segment`() {
        val binding = NavigationPathBinding(
            keyType = NavigationKey::class,
            pattern = "test",
            deserialize = { ObjectKey },
            serialize = { TODO() },
        )

        assertEquals(ObjectKey, binding.fromPath(ParsedPath.fromString("/test")))
        assertEquals(ObjectKey, binding.fromPath(ParsedPath.fromString("/test/")))
        assertEquals(ObjectKey, binding.fromPath(ParsedPath.fromString("test")))
    }

    @Test
    fun `fromPath for multi segment`() {
        val binding = NavigationPathBinding(
            keyType = NavigationKey::class,
            pattern = "test/example/next",
            deserialize = { ObjectKey },
            serialize = { TODO() },
        )

        assertEquals(ObjectKey, binding.fromPath(ParsedPath.fromString("/test/example/next")))
        assertEquals(ObjectKey, binding.fromPath(ParsedPath.fromString("/test/example/next/")))
        assertEquals(ObjectKey, binding.fromPath(ParsedPath.fromString("test/example/next")))
    }

    @Test
    fun `fromPath for multi segment with placeholders`() {
        val binding = NavigationPathBinding(
            keyType = NavigationKey::class,
            pattern = "test/{id}/example/{name}",
            deserialize = {
                ParameterizedKey(
                    id = require("id"),
                    name = require("name"),
                )
            },
            serialize = { TODO() },
        )

        assertEquals(
            ParameterizedKey(id = "123", name = "john"),
            binding.fromPath(ParsedPath.fromString("/test/123/example/john")),
        )
    }

    @Test
    fun `fromPath for multi segment with optional parameters`() {
        val binding = NavigationPathBinding(
            keyType = NavigationKey::class,
            pattern = "test/{id}/example/{name}?required={required}&optional={optional?}",
            deserialize = {
                ParameterizedOptionalKey(
                    id = require("id"),
                    name = require("name"),
                    requiredQuery = require("required"),
                    optionalQuery = optional("optional"),
                )
            },
            serialize = { TODO() },
        )

        assertEquals(
            ParameterizedOptionalKey(
                id = "123",
                name = "john",
                requiredQuery = "456",
                optionalQuery = null,
            ),
            binding.fromPath(ParsedPath.fromString("/test/123/example/john?required=456")),
        )

        assertEquals(
            ParameterizedOptionalKey(
                id = "123",
                name = "john",
                requiredQuery = "456",
                optionalQuery = "768",
            ),
            binding.fromPath(ParsedPath.fromString("/test/123/example/john?required=456&optional=768")),
        )
    }

    @Test
    fun `toPath for root`() {
        val binding = NavigationPathBinding(
            keyType = ObjectKey::class,
            pattern = "/",
            deserialize = { TODO() },
            serialize = {},
        )

        assertEquals("/", binding.toPath(ObjectKey))
    }

    @Test
    fun `toPath for single segment`() {
        val binding = NavigationPathBinding(
            keyType = ObjectKey::class,
            pattern = "test",
            deserialize = { TODO() },
            serialize = {},
        )

        assertEquals("/test", binding.toPath(ObjectKey))
    }

    @Test
    fun `toPath for multi segment`() {
        val binding = NavigationPathBinding(
            keyType = ObjectKey::class,
            pattern = "test/example/next",
            deserialize = { TODO() },
            serialize = {},
        )

        assertEquals("/test/example/next", binding.toPath(ObjectKey))
    }

    @Test
    fun `toPath for multi segment with placeholders`() {
        val binding = NavigationPathBinding(
            keyType = ParameterizedKey::class,
            pattern = "test/{id}/example/{name}",
            deserialize = { TODO() },
            serialize = {
                set("id", it.id)
                set("name", it.name)
            },
        )

        assertEquals("/test/123/example/john", binding.toPath(ParameterizedKey("123", "john")))
    }

    @Test
    fun `toPath for multi segment with optional parameters`() {
        val binding = NavigationPathBinding(
            keyType = ParameterizedOptionalKey::class,
            pattern = "test/{id}/example/{name}?required={required}&optional={optional?}",
            deserialize = { TODO() },
            serialize = {
                set("id", it.id)
                set("name", it.name)
                set("required", it.requiredQuery)
                if (it.optionalQuery != null) {
                    set("optional", it.optionalQuery)
                }
            },
        )

        assertEquals(
            "/test/123/example/john?required=456",
            binding.toPath(
                ParameterizedOptionalKey(
                    id = "123",
                    name = "john",
                    requiredQuery = "456",
                    optionalQuery = null
                )
            ),
        )

        assertEquals(
            "/test/123/example/john?required=456&optional=768",
            binding.toPath(
                ParameterizedOptionalKey(
                    id = "123",
                    name = "john",
                    requiredQuery = "456",
                    optionalQuery = "768"
                )
            ),
        )
    }

    @Test
    fun `fromPath for multi segment with url encoded characters`() {
        val binding = NavigationPathBinding(
            keyType = ParameterizedOptionalKey::class,
            pattern = "test/{id}/example/{name}?required={required}&optional={optional?}",
            deserialize = {
                ParameterizedOptionalKey(
                    id = require("id"),
                    name = require("name"),
                    requiredQuery = require("required"),
                    optionalQuery = optional("optional")
                )
            },
            serialize = { TODO() },
        )

        assertEquals(
            ParameterizedOptionalKey(
                id = "⛅︎☂︎♠︎ spaces ♛☹︎✎",
                name = "😀 / 🤪 - 🤩 ℔ℑ∩∀∁",
                requiredQuery = "😇🥰℀ℳ℃",
                optionalQuery = "- dashes / slashes {} [%20%asd] "
            ),
            binding.fromPath(
                ParsedPath.fromString(
                    "/test/%E2%9B%85%EF%B8%8E%E2%98%82%EF%B8%8E%E2%99%A0%EF%B8%8E%20spaces%20%E2%99%9B%E2%98%B9%EF%B8%8E%E2%9C%8E/example/%F0%9F%98%80%20%2F%20%F0%9F%A4%AA%20-%20%F0%9F%A4%A9%20%E2%84%94%E2%84%91%E2%88%A9%E2%88%80%E2%88%81?required=%F0%9F%98%87%F0%9F%A5%B0%E2%84%80%E2%84%B3%E2%84%83&optional=-%20dashes%20%2F%20slashes%20%7B%7D%20%5B%2520%25asd%5D%20"
                )
            )
        )
    }

    @Test
    fun `toPath for multi segment with optional parameters and url encoded characters`() {
        val binding = NavigationPathBinding(
            keyType = ParameterizedOptionalKey::class,
            pattern = "test/{id}/example/{name}?required={required}&optional={optional?}",
            deserialize = { TODO() },
            serialize = {
                set("id", it.id)
                set("name", it.name)
                set("required", it.requiredQuery)
                if (it.optionalQuery != null) {
                    set("optional", it.optionalQuery)
                }
            },
        )

        assertEquals(
            "/test/%E2%9B%85%EF%B8%8E%E2%98%82%EF%B8%8E%E2%99%A0%EF%B8%8E%20spaces%20%E2%99%9B%E2%98%B9%EF%B8%8E%E2%9C%8E/example/%F0%9F%98%80%20%2F%20%F0%9F%A4%AA%20-%20%F0%9F%A4%A9%20%E2%84%94%E2%84%91%E2%88%A9%E2%88%80%E2%88%81?required=%F0%9F%98%87%F0%9F%A5%B0%E2%84%80%E2%84%B3%E2%84%83&optional=-%20dashes%20%2F%20slashes%20%7B%7D%20%5B%2520%25asd%5D%20",
            binding.toPath(
                ParameterizedOptionalKey(
                    id = "⛅︎☂︎♠︎ spaces ♛☹︎✎",
                    name = "😀 / 🤪 - 🤩 ℔ℑ∩∀∁",
                    requiredQuery = "😇🥰℀ℳ℃",
                    optionalQuery = "- dashes / slashes {} [%20%asd] "
                )
            ),
        )
    }

    @Test
    fun `createPathBinding for no params`() {
        val binding = NavigationPathBinding.createPathBinding(
            "test",
            { ParameterKeys.NoParams }
        )
        val expectedKey = ParameterKeys.NoParams

        val path = binding.toPath(expectedKey)
        val parsedKey = binding.fromPath(ParsedPath.fromString(path))

        assertEquals("/test", path)
        assertEquals(expectedKey, parsedKey)
    }

    @Test
    fun `createPathBinding for one param`() {
        val binding = NavigationPathBinding.createPathBinding(
            "test/{id}",
            ParameterKeys.OneParam::id,
            ParameterKeys::OneParam,
        )
        val expectedKey = ParameterKeys.OneParam("123")

        val path = binding.toPath(expectedKey)
        val parsedKey = binding.fromPath(ParsedPath.fromString(path))

        assertEquals("/test/123", path)
        assertEquals(expectedKey, parsedKey)
    }

    @Test
    fun `createPathBinding for two params`() {
        val binding = NavigationPathBinding.createPathBinding(
            "test/{id}/example/{name}",
            ParameterKeys.TwoParams::id,
            ParameterKeys.TwoParams::name,
            ParameterKeys::TwoParams,
        )
        val expectedKey = ParameterKeys.TwoParams("123", "john")

        val path = binding.toPath(expectedKey)
        val parsedKey = binding.fromPath(ParsedPath.fromString(path))

        assertEquals("/test/123/example/john", path)
        assertEquals(expectedKey, parsedKey)
    }

    @Test
    fun `createPathBinding for three params`() {
        val binding = NavigationPathBinding.createPathBinding(
            "test/{id}/example/{name}?queryAge={age}",
            ParameterKeys.ThreeParams::id,
            ParameterKeys.ThreeParams::name,
            ParameterKeys.ThreeParams::age,
            ParameterKeys::ThreeParams,
        )
        val expectedKey = ParameterKeys.ThreeParams("123", "john", 30)

        val path = binding.toPath(expectedKey)
        val parsedKey = binding.fromPath(ParsedPath.fromString(path))

        assertEquals("/test/123/example/john?queryAge=30", path)
        assertEquals(expectedKey, parsedKey)
    }

    @Test
    fun `createPathBinding for four params`() {
        val binding = NavigationPathBinding.createPathBinding(
            "test/{id}/example/{name}?queryAge={age}&isActive={isActive}",
            ParameterKeys.FourParams::id,
            ParameterKeys.FourParams::name,
            ParameterKeys.FourParams::age,
            ParameterKeys.FourParams::isActive,
            ParameterKeys::FourParams,
        )
        val expectedKey = ParameterKeys.FourParams("123", "john", 30, true)

        val path = binding.toPath(expectedKey)
        val parsedKey = binding.fromPath(ParsedPath.fromString(path))

        assertEquals("/test/123/example/john?queryAge=30&isActive=true", path)
        assertEquals(expectedKey, parsedKey)
    }

    @Test
    fun `createPathBinding for five params`() {
        val binding = NavigationPathBinding.createPathBinding(
            "test/{id}/example/{name}?queryAge={age}&isActive={isActive}&address={address?}",
            ParameterKeys.FiveParams::id,
            ParameterKeys.FiveParams::name,
            ParameterKeys.FiveParams::age,
            ParameterKeys.FiveParams::isActive,
            ParameterKeys.FiveParams::address,
            ParameterKeys::FiveParams,
        )
        val expectedKey = ParameterKeys.FiveParams("123", "john", 30, true, "123 Main St")

        val path = binding.toPath(expectedKey)
        val parsedKey = binding.fromPath(ParsedPath.fromString(path))

        assertEquals("/test/123/example/john?queryAge=30&isActive=true&address=123%20Main%20St", path)
        assertEquals(expectedKey, parsedKey)
    }

    @Test
    fun `createPathBinding for six params`() {
        val binding = NavigationPathBinding.createPathBinding(
            "test/{id}/example/{name}?queryAge={age}&isActive={isActive}&address={address}&phoneNumber={phoneNumber}",
            ParameterKeys.SixParams::id,
            ParameterKeys.SixParams::name,
            ParameterKeys.SixParams::age,
            ParameterKeys.SixParams::isActive,
            ParameterKeys.SixParams::address,
            ParameterKeys.SixParams::phoneNumber,
            ParameterKeys::SixParams,
        )
        val expectedKey = ParameterKeys.SixParams(
            id = "123",
            name = "john",
            age = 30,
            isActive = true,
            address = "123 Main St",
            phoneNumber = "123-456-7890"
        )

        val path = binding.toPath(expectedKey)
        val parsedKey = binding.fromPath(ParsedPath.fromString(path))

        assertEquals("/test/123/example/john?queryAge=30&isActive=true&address=123%20Main%20St&phoneNumber=123-456-7890", path)
        assertEquals(expectedKey, parsedKey)
    }

    @Test
    fun `createPathBinding for seven params`() {
        val binding = NavigationPathBinding.createPathBinding(
            "test/{id}/example/{name}?queryAge={age}&isActive={isActive}&address={address}&phoneNumber={phoneNumber}&email={email}",
            ParameterKeys.SevenParams::id,
            ParameterKeys.SevenParams::name,
            ParameterKeys.SevenParams::age,
            ParameterKeys.SevenParams::isActive,
            ParameterKeys.SevenParams::address,
            ParameterKeys.SevenParams::phoneNumber,
            ParameterKeys.SevenParams::email,
            ParameterKeys::SevenParams,
        )
        val expectedKey = ParameterKeys.SevenParams(
            id = "123",
            name = "john",
            age = 30,
            isActive = true,
            address = "123 Main St",
            phoneNumber = "123-456-7890",
            email = "test@example.com",
        )
        val path = binding.toPath(expectedKey)
        val parsedKey = binding.fromPath(ParsedPath.fromString(path))
        assertEquals(
            "/test/123/example/john?queryAge=30&isActive=true&address=123%20Main%20St&phoneNumber=123-456-7890&email=test%40example.com",
            path
        )
        assertEquals(expectedKey, parsedKey)
    }

    @Test
    fun `createPathBinding for eight params`() {
        val binding = NavigationPathBinding.createPathBinding(
            "test/{id}/example/{name}?queryAge={age}&isActive={isActive}&address={address}&phoneNumber={phoneNumber}&email={email}&website={website}",
            ParameterKeys.EightParams::id,
            ParameterKeys.EightParams::name,
            ParameterKeys.EightParams::age,
            ParameterKeys.EightParams::isActive,
            ParameterKeys.EightParams::address,
            ParameterKeys.EightParams::phoneNumber,
            ParameterKeys.EightParams::email,
            ParameterKeys.EightParams::website,
            ParameterKeys::EightParams,
        )
        val expectedKey = ParameterKeys.EightParams(
            id = "123",
            name = "john",
            age = 30,
            isActive = true,
            address = "123 Main St",
            phoneNumber = "123-456-7890",
            email = "test@example.com",
            website = "https://example.com",
        )
        val path = binding.toPath(expectedKey)
        val parsedKey = binding.fromPath(ParsedPath.fromString(path))
        assertEquals(
            "/test/123/example/john?queryAge=30&isActive=true&address=123%20Main%20St&phoneNumber=123-456-7890&email=test%40example.com&website=https%3A%2F%2Fexample.com",
            path
        )
        assertEquals(expectedKey, parsedKey)
    }
}

private data object ObjectKey : NavigationKey

data class ParameterizedKey(
    val id: String,
    val name: String,
) : NavigationKey

data class ParameterizedOptionalKey(
    val id: String,
    val name: String,
    val requiredQuery: String,
    val optionalQuery: String?,
) : NavigationKey


object ParameterKeys {
    object NoParams : NavigationKey

    data class OneParam(
        val id: String
    ) : NavigationKey

    data class TwoParams(
        val id: String,
        val name: String
    ) : NavigationKey

    data class ThreeParams(
        val id: String,
        val name: String,
        val age: Int
    ) : NavigationKey

    data class FourParams(
        val id: String,
        val name: String,
        val age: Int,
        val isActive: Boolean
    ) : NavigationKey

    data class FiveParams(
        val id: String,
        val name: String,
        val age: Int,
        val isActive: Boolean,
        val address: String?
    ) : NavigationKey

    data class SixParams(
        val id: String,
        val name: String,
        val age: Int,
        val isActive: Boolean,
        val address: String,
        val phoneNumber: String
    ) : NavigationKey

    data class SevenParams(
        val id: String,
        val name: String,
        val age: Int,
        val isActive: Boolean,
        val address: String,
        val phoneNumber: String,
        val email: String
    ) : NavigationKey

    data class EightParams(
        val id: String,
        val name: String,
        val age: Int,
        val isActive: Boolean,
        val address: String,
        val phoneNumber: String,
        val email: String,
        val website: String
    ) : NavigationKey
}