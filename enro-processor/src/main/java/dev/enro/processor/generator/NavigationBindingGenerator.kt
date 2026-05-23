package dev.enro.processor.generator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.annotations.GeneratedNavigationBinding
import dev.enro.annotations.NavigationPath
import dev.enro.processor.domain.DestinationReference
import dev.enro.processor.extensions.EnroLocation
import dev.enro.processor.extensions.toDisplayString

object NavigationBindingGenerator {

    @OptIn(KspExperimental::class)
    fun generate(
        environment: SymbolProcessorEnvironment,
        resolver: Resolver,
        destinationDeclaration: KSDeclaration,
    ) {
        val destination = DestinationReference(resolver, destinationDeclaration)

        if (destination.isProperty) {
            val propertyClassDeclaration = destination.keyTypeFromPropertyProvider
            if (propertyClassDeclaration == null) {
                environment.logger.error("Cannot find property type for ${destinationDeclaration.simpleName.asString()}")
                return
            }
            val propertyType = propertyClassDeclaration
            if (!destination.keyType.asStarProjectedType().isAssignableFrom(propertyType)) {
                environment.logger.error(
                    message = "${destinationDeclaration.simpleName.asString()} is annotated with @NavigationDestination(${destination.keyType.toDisplayString()}::class) but is a NavigationDestinationProvider<${propertyType.toDisplayString()}>",
                    symbol = destinationDeclaration,
                )
                return
            }
        }

        val typeSpec = TypeSpec.classBuilder(destination.bindingName)
            .addModifiers(KModifier.PUBLIC)
            .addSuperinterface(
                ClassName("dev.enro.controller", "NavigationModuleAction")
            )
            .addAnnotation(
                AnnotationSpec.builder(GeneratedNavigationBinding::class.java)
                    .addMember(
                        "destination = %L",
                        CodeBlock.of("\"${requireNotNull(destinationDeclaration.qualifiedName).asString()}\"")
                    )
                    .addMember(
                        "navigationKey = %L",
                        CodeBlock.of("\"${requireNotNull(destination.keyType.qualifiedName).asString()}\"")
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("invoke")
                    .receiver(ClassName("dev.enro.controller", "NavigationModule.BuilderScope"))
                    .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                    .returns(Unit::class.java)
                    .addNavigationDestination(
                        environment = environment,
                        destination = destination,
                    )
                    .addPathBinding(
                        environment = environment,
                        destination = destination,
                    )
                    .build()
            )
            .build()

        FileSpec
            .builder(EnroLocation.GENERATED_PACKAGE, requireNotNull(typeSpec.name))
            .addType(typeSpec)
            .addImport(
                destinationDeclaration.packageName.asString(),
                requireNotNull(destinationDeclaration.qualifiedName).asString()
                    .removePrefix(destinationDeclaration.packageName.asString())
            )
            .addImport("dev.enro.controller", "NavigationModule")
            .addImport("dev.enro.ui", "navigationDestination")
            .addImport("dev.enro.path", "NavigationPathBinding")
            .addImport("dev.enro.path", "createPathBinding")
            .addImport("dev.enro.path", "fromBinding")
            .apply {
                when {
                    destination.isActivity -> addImport("dev.enro.ui.destinations", "activityDestination")
                    destination.isFragment -> addImport("dev.enro.ui.destinations", "fragmentDestination")
                }
            }
            .build()
            .writeTo(
                codeGenerator = environment.codeGenerator,
                dependencies = Dependencies(
                    aggregating = false,
                    sources = arrayOf(requireNotNull(destinationDeclaration.containingFile)),
                )
            )
    }

    private fun FunSpec.Builder.addNavigationDestination(
        environment: SymbolProcessorEnvironment,
        destination: DestinationReference,
    ): FunSpec.Builder {
        val formatting = LinkedHashMap<String, Any>()
        formatting["keyType"] = destination.keyType.asStarProjectedType().toTypeName()
        formatting["keyName"] = destination.keyType.toClassName()
        
        val destinationName = when {
            destination.isClass -> {
                formatting["destinationType"] = destination.toClassName()
                "%destinationType:T"
            }
            destination.isProperty -> {
                formatting["destinationProperty"] = destination.declaration.simpleName.asString()
                "%destinationProperty:L"
            }
            destination.isFunction -> {
                formatting["destinationFun"] = destination.declaration.simpleName.asString()
                "%destinationFun:L"
            }

            else -> {
                environment.logger.error(
                    "Could not generate NavigationDestination for ${destination.declaration.qualifiedName?.asString()}. " +
                            "This is likely because the destination is not a class or function."
                )
                "INVALID_DESTINATION"
            }
        }
        val platformOverride = when(destination.isPlatformOverride) {
            true -> ", isPlatformOverride = true"
            else -> ""
        }
        when {
            destination.isClass -> when {
                destination.isFragment -> addNamedCode(
                    "destination(fragmentDestination(%keyType:T::class, %destinationType:T::class)$platformOverride)",
                    formatting,
                )
                destination.isActivity -> addNamedCode(
                    "destination(activityDestination(%keyType:T::class, %destinationType:T::class)$platformOverride)",
                    formatting,
                )
                else ->  environment.logger.error(
                    "${destination.declaration.qualifiedName?.asString()} is not a valid enro class destination."
                )
            }
            destination.isProperty -> addNamedCode(
                "destination($destinationName$platformOverride)",
                formatting,
            )
            destination.isComposable -> addNamedCode(
                "destination(navigationDestination<%keyType:T> { $destinationName() }$platformOverride)",
                formatting,
            )
            else -> {
                environment.logger.error(
                    "${destination.declaration.qualifiedName?.asString()} is not a valid navigation destination for Enro."
                )
            }
        }

        return this
    }

    @OptIn(KspExperimental::class, ExperimentalEnroApi::class)
    private fun FunSpec.Builder.addPathBinding(
        environment: SymbolProcessorEnvironment,
        destination: DestinationReference,
    ): FunSpec.Builder {
        val navigationPaths = destination.keyType.getAnnotationsByType(NavigationPath::class)
            .map {
                val isObject = destination.keyType.classKind == ClassKind.OBJECT
                val constructor = if (isObject) null else destination.keyType.primaryConstructor
                return@map it to constructor
            }
            .plus(
                destination.keyType.getConstructors()
                    .flatMap { constructor ->
                        constructor.getAnnotationsByType(NavigationPath::class).toList().map {
                            it to constructor
                        }
                    }
            )
            .toList()

        navigationPaths.forEach { (path, constructor) ->
            val pattern = path.pattern
            val params = pathPatternToParameterNames(pattern)

            val hasValueClassParam = constructor != null && constructor.parameters.any { param ->
                val paramName = param.name?.asString() ?: return@any false
                if (paramName !in params) return@any false
                param.type.resolve().declaration.isValueClass()
            }
            val exceedsHelperArity = params.size > 8

            if (constructor != null && (hasValueClassParam || exceedsHelperArity)) {
                addExplicitPathBinding(environment, destination, pattern, constructor)
            } else {
                addHelperPathBinding(destination, pattern, constructor)
            }
        }

        addFromBindingPaths(environment, destination)
        return this
    }

    @OptIn(KspExperimental::class)
    private fun FunSpec.Builder.addFromBindingPaths(
        environment: SymbolProcessorEnvironment,
        destination: DestinationReference,
    ): FunSpec.Builder {
        val fromBindingAnnotations = destination.keyType.annotations
            .filter { annotation ->
                annotation.annotationType.resolve().declaration.qualifiedName?.asString() ==
                    "dev.enro.annotations.NavigationPath.FromBinding"
            }
            .toList()

        if (fromBindingAnnotations.isEmpty()) return this

        val pathBindingFqn = "dev.enro.NavigationKey.PathBinding"

        fromBindingAnnotations.forEach { annotation ->
            val bindingTypeArg = annotation.arguments
                .firstOrNull { it.name?.asString() == "binding" }
                ?.value as? KSType
            val bindingDecl = bindingTypeArg?.declaration as? KSClassDeclaration
            if (bindingDecl == null) {
                environment.logger.error(
                    "@NavigationPath.FromBinding could not resolve binding class on ${destination.keyType.qualifiedName?.asString()}",
                    destination.declaration,
                )
                return@forEach
            }

            if (bindingDecl.classKind != ClassKind.OBJECT) {
                environment.logger.error(
                    "@NavigationPath.FromBinding referenced class '${bindingDecl.qualifiedName?.asString()}' must be an object (singleton).",
                    bindingDecl,
                )
                return@forEach
            }

            val implementsPathBinding = bindingDecl.getAllSuperTypes().any { superType ->
                val decl = superType.declaration
                if (decl.qualifiedName?.asString() != pathBindingFqn) return@any false
                val typeArg = superType.arguments.firstOrNull()?.type?.resolve()
                typeArg?.declaration?.qualifiedName?.asString() ==
                    destination.keyType.qualifiedName?.asString()
            }
            if (!implementsPathBinding) {
                environment.logger.error(
                    "@NavigationPath.FromBinding referenced class '${bindingDecl.qualifiedName?.asString()}' " +
                        "must implement NavigationKey.PathBinding<${destination.keyType.qualifiedName?.asString()}>.",
                    bindingDecl,
                )
                return@forEach
            }

            val formatting = LinkedHashMap<String, Any>()
            formatting["keyType"] = destination.keyType.toClassName()
            formatting["binding"] = bindingDecl.toClassName()

            addCode("\n")
            addNamedCode(
                """
                path(
                    NavigationPathBinding.fromBinding(
                        keyType = %keyType:T::class,
                        binding = %binding:T,
                    )
                )
               """.trimIndent(),
                formatting,
            )
        }
        return this
    }

    private fun FunSpec.Builder.addHelperPathBinding(
        destination: DestinationReference,
        pattern: String,
        constructor: KSFunctionDeclaration?,
    ): FunSpec.Builder {
        val constructorReference = when {
            constructor == null -> "{ %T }"
            else -> "::%T"
        }

        addCode("\n")
        val params = pathPatternToParameterNames(pattern)

        val typeName = destination.keyType.asStarProjectedType().toTypeName()
        val paramTypeArray = params.map { typeName }.toTypedArray()
        val paramReferences = params.map { "%T::$it" }.joinToString("\n") {
            "                        $it,"
        }

        addCode(
            """
            path(
                NavigationPathBinding.createPathBinding(
                    pattern = %S,${"\n"}$paramReferences
                    constructor = $constructorReference
                )
            )
           """.trimIndent(),
            pattern,
            *paramTypeArray,
            typeName,
        )
        return this
    }

    private fun FunSpec.Builder.addExplicitPathBinding(
        environment: SymbolProcessorEnvironment,
        destination: DestinationReference,
        pattern: String,
        constructor: KSFunctionDeclaration,
    ): FunSpec.Builder {
        val patternParams = pathPatternToParameterNames(pattern)
        val optionalParams = pathPatternToOptionalParameterNames(pattern)
        val keyTypeName = destination.keyType.toClassName()

        // Resolve each pattern param to its constructor param + serialization shape.
        val resolved = patternParams.mapNotNull { paramName ->
            val ksParam = constructor.parameters.firstOrNull { it.name?.asString() == paramName }
            if (ksParam == null) {
                environment.logger.error(
                    "Path pattern parameter '$paramName' does not match any parameter on the annotated constructor of ${destination.keyType.qualifiedName?.asString()}",
                    constructor,
                )
                return@mapNotNull null
            }
            val ksType = ksParam.type.resolve()
            val isOptional = paramName in optionalParams
            val nullable = ksType.isMarkedNullable
            if (nullable != isOptional) {
                environment.logger.error(
                    "Path pattern parameter '$paramName' is ${if (isOptional) "optional" else "required"}, " +
                        "but constructor parameter is ${if (nullable) "nullable" else "non-nullable"}",
                    ksParam,
                )
                return@mapNotNull null
            }
            val shape = resolvePathParamShape(environment, ksType, ksParam)
                ?: return@mapNotNull null
            ResolvedPathParam(paramName, shape, nullable)
        }
        if (resolved.size != patternParams.size) return this

        // Build deserialize body: K(p1 = ..., p2 = ...)
        // Build serialize body: set(...), key.x?.let { ... }
        val deserializeLines = mutableListOf<String>()
        val serializeLines = mutableListOf<String>()
        val codeArgs = mutableListOf<Any>()

        deserializeLines.add("%keyType:T(")
        resolved.forEachIndexed { index, p ->
            val deserializeExpr = buildDeserializeExpr(p, codeArgs)
            deserializeLines.add("    ${p.name} = $deserializeExpr,")
            val serializeStmt = buildSerializeStmt(p, codeArgs)
            serializeLines.add(serializeStmt)
        }
        deserializeLines.add(")")

        val deserializeBody = deserializeLines.joinToString("\n                            ")
        val serializeBody = serializeLines.joinToString("\n                        ")

        val formatting = LinkedHashMap<String, Any>()
        formatting["keyType"] = keyTypeName
        formatting["pattern"] = pattern
        codeArgs.forEachIndexed { i, value -> formatting["arg$i"] = value }

        addCode("\n")
        addNamedCode(
            """
            path(
                NavigationPathBinding(
                    keyType = %keyType:T::class,
                    pattern = %pattern:S,
                    deserialize = {
                        $deserializeBody
                    },
                    serialize = {
                        $serializeBody
                    },
                )
            )
           """.trimIndent(),
            formatting,
        )
        return this
    }
}

private data class PathParamShape(
    val primitiveFqn: String,
    val valueClassTypeName: TypeName?,
    val valueClassUnderlyingPropertyName: String?,
)

private data class ResolvedPathParam(
    val name: String,
    val shape: PathParamShape,
    val isNullable: Boolean,
)

private fun resolvePathParamShape(
    environment: SymbolProcessorEnvironment,
    ksType: KSType,
    sourceSymbol: com.google.devtools.ksp.symbol.KSNode,
): PathParamShape? {
    val decl = ksType.declaration
    val primitiveFqn = decl.qualifiedName?.asString()
    if (primitiveFqn in SUPPORTED_PRIMITIVE_FQNS) {
        return PathParamShape(
            primitiveFqn = primitiveFqn!!,
            valueClassTypeName = null,
            valueClassUnderlyingPropertyName = null,
        )
    }
    if (!decl.isValueClass()) {
        environment.logger.error(
            "Path parameter type '${primitiveFqn ?: decl.simpleName.asString()}' is not supported. " +
                "Must be a primitive (String, Int, Long, Float, Double, Short, Byte, Char, Boolean) " +
                "or a value class wrapping one of those primitives.",
            sourceSymbol,
        )
        return null
    }
    val valueClass = decl as KSClassDeclaration
    val underlyingParam = valueClass.primaryConstructor?.parameters?.singleOrNull()
    if (underlyingParam == null) {
        environment.logger.error(
            "Value class '${valueClass.qualifiedName?.asString()}' must have exactly one constructor parameter to be used as a path parameter.",
            sourceSymbol,
        )
        return null
    }
    val underlyingType = underlyingParam.type.resolve()
    if (underlyingType.isMarkedNullable) {
        environment.logger.error(
            "Value class '${valueClass.qualifiedName?.asString()}' wraps a nullable type, which is not supported for path parameters.",
            sourceSymbol,
        )
        return null
    }
    val underlyingFqn = underlyingType.declaration.qualifiedName?.asString()
    if (underlyingFqn !in SUPPORTED_PRIMITIVE_FQNS) {
        environment.logger.error(
            "Value class '${valueClass.qualifiedName?.asString()}' wraps unsupported type '$underlyingFqn'. " +
                "Only value classes wrapping a primitive (String, Int, Long, Float, Double, Short, Byte, Char, Boolean) are supported.",
            sourceSymbol,
        )
        return null
    }
    return PathParamShape(
        primitiveFqn = underlyingFqn!!,
        valueClassTypeName = valueClass.toClassName(),
        valueClassUnderlyingPropertyName = underlyingParam.name?.asString(),
    )
}

private fun buildDeserializeExpr(
    param: ResolvedPathParam,
    codeArgs: MutableList<Any>,
): String {
    val accessor = if (param.isNullable) {
        "optional(\"${param.name}\")"
    } else {
        "require(\"${param.name}\")"
    }
    val converted = primitiveParseExpression(param.shape.primitiveFqn, accessor, param.isNullable)
    return if (param.shape.valueClassTypeName != null) {
        codeArgs.add(param.shape.valueClassTypeName)
        val placeholder = "%arg${codeArgs.size - 1}:T"
        if (param.isNullable) {
            "$converted?.let { $placeholder(it) }"
        } else {
            "$placeholder($converted)"
        }
    } else {
        converted
    }
}

private fun buildSerializeStmt(
    param: ResolvedPathParam,
    codeArgs: MutableList<Any>,
): String {
    val unwrap = if (param.shape.valueClassUnderlyingPropertyName != null) {
        "v.${param.shape.valueClassUnderlyingPropertyName}"
    } else {
        "v"
    }
    val stringified = if (param.shape.primitiveFqn == "kotlin.String") unwrap else "$unwrap.toString()"
    return if (param.isNullable) {
        "it.${param.name}?.let { v -> set(\"${param.name}\", $stringified) }"
    } else {
        val nonNullSource = if (param.shape.valueClassUnderlyingPropertyName != null) {
            "it.${param.name}.${param.shape.valueClassUnderlyingPropertyName}"
        } else {
            "it.${param.name}"
        }
        val nonNullStringified = if (param.shape.primitiveFqn == "kotlin.String") nonNullSource else "$nonNullSource.toString()"
        "set(\"${param.name}\", $nonNullStringified)"
    }
}

private fun primitiveParseExpression(
    primitiveFqn: String,
    source: String,
    isNullable: Boolean,
): String {
    val nullableSuffix = if (isNullable) "?" else ""
    return when (primitiveFqn) {
        "kotlin.String" -> source
        "kotlin.Int" -> "$source$nullableSuffix.toInt()"
        "kotlin.Long" -> "$source$nullableSuffix.toLong()"
        "kotlin.Float" -> "$source$nullableSuffix.toFloat()"
        "kotlin.Double" -> "$source$nullableSuffix.toDouble()"
        "kotlin.Short" -> "$source$nullableSuffix.toShort()"
        "kotlin.Byte" -> "$source$nullableSuffix.toByte()"
        "kotlin.Char" -> "$source$nullableSuffix.first()"
        "kotlin.Boolean" -> "$source$nullableSuffix.toBoolean()"
        else -> error("Unsupported primitive: $primitiveFqn")
    }
}

private fun KSDeclaration.isValueClass(): Boolean {
    return this is KSClassDeclaration && Modifier.VALUE in modifiers
}

private val SUPPORTED_PRIMITIVE_FQNS = setOf(
    "kotlin.String",
    "kotlin.Int",
    "kotlin.Long",
    "kotlin.Float",
    "kotlin.Double",
    "kotlin.Short",
    "kotlin.Byte",
    "kotlin.Char",
    "kotlin.Boolean",
)

private fun pathPatternToOptionalParameterNames(pattern: String): Set<String> {
    val split = pattern.split("?", limit = 2)
    val query = split.getOrNull(1) ?: return emptySet()
    return query.split("&")
        .mapNotNull { it.split("=", limit = 2).getOrNull(1) }
        .filter { it.startsWith("{") && it.endsWith("?}") }
        .map { it.removePrefix("{").removeSuffix("?}") }
        .toSet()
}

private fun pathPatternToParameterNames(pattern: String): List<String> {
    val split = pattern.split("?", limit = 2)
    val path = split[0]
    val query = if (split.size > 1) split[1] else null
    val pathParameters = path
        .split("/")
        .filter { it.startsWith("{") && it.endsWith("}") }

    val queryParameters = query?.split("&")
        .orEmpty()
        .mapNotNull { it.split("=", limit = 2).getOrNull(1) }
        .filter { it.startsWith("{") && it.endsWith("}") }

    return (pathParameters + queryParameters)
        .map {
            it.removePrefix("{")
                .removeSuffix("}")
                .removeSuffix("?")
        }
        .filter { it.isNotEmpty() }
}