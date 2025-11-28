package dev.enro.compiler.fir

import dev.enro.compiler.EnroLogger
import dev.enro.compiler.fir.generators.NavigationBindingGenerator
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFunctionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class EnroFirExtensionRegistrar(
    private val logger: EnroLogger,
) : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        +::FirEnroCheckers
        +FirDeclarationGenerationExtension.Factory { session ->
            NavigationBindingGenerator(
                session = session,
                logger = logger,
            )
        }
    }

    class FirEnroCheckers(
        session: FirSession,
    ) : FirAdditionalCheckersExtension(session) {
        override val declarationCheckers: DeclarationCheckers =
            object : DeclarationCheckers() {
                override val classCheckers: Set<FirClassChecker>
                    get() = setOf(EnroClassChecker())

                override val functionCheckers: Set<FirFunctionChecker>
                    get() = setOf(EnroFunctionChecker())

                override val propertyCheckers: Set<FirPropertyChecker>
                    get() = setOf(EnroPropertyChecker())
            }


        class EnroClassChecker() : FirClassChecker(MppCheckerKind.Common) {
            context(_: CheckerContext, _: DiagnosticReporter)
            override fun check(declaration: FirClass) {
                processStatement(declaration)
            }
        }

        class EnroFunctionChecker() : FirFunctionChecker(MppCheckerKind.Common) {
            context(_: CheckerContext, _: DiagnosticReporter)
            override fun check(
                declaration: FirFunction,
            ) {
                processStatement(declaration)
            }
        }

        class EnroPropertyChecker() : FirPropertyChecker(MppCheckerKind.Common) {
            context(_: CheckerContext, _: DiagnosticReporter)
            override fun check(
                declaration: FirProperty,
            ) {
                processStatement(declaration)
            }
        }

    }
}

private fun processStatement(
    statement: FirStatement,
) {
//    val navigationDestination = statement.getAnnotation<NavigationDestination>(session)
//        ?: return
//    val navigationKeyName = navigationDestination.getKClassArgument(Name.identifier("key"), session)
//    if (navigationKeyName == null) error("failed to find key argument")
}