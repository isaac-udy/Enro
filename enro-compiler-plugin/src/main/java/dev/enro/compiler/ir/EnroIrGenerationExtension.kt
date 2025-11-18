package dev.enro.compiler.ir

import dev.enro.compiler.EnroLogger
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.konan.descriptors.allContainingDeclarations
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptorVisitor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageViewDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.PropertyGetterDescriptor
import org.jetbrains.kotlin.descriptors.PropertySetterDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.ScriptDescriptor
import org.jetbrains.kotlin.descriptors.TypeAliasDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.descriptors.findPackage
import org.jetbrains.kotlin.descriptors.impl.DeclarationDescriptorVisitorEmptyBodies
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.IrVisitor
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import kotlin.math.log

class EnroIrGenerationExtension(
    private val logger: EnroLogger
) : IrGenerationExtension {

    @OptIn(InternalSymbolFinderAPI::class)
    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext
    ) {
        logger.warn("Generating IR for module: ${moduleFragment.name}")
        logger.warn(" Saw dependency modules: ${moduleFragment.descriptor} ${moduleFragment.descriptor.allDependencyModules}")
        logger.warn(" Saw dependency modules: ${
            pluginContext.referenceClass(
                ClassId.fromString(
                    "enro_generated_bindings/_dev_enro_tests_module_ModuleOneScreen_GeneratedNavigationBinding"
                ))
        }")
        pluginContext.referenceFunctions(
            CallableId(
                packageName = FqName("dev.enro.tests.module"),
                callableName = Name.identifier("bind")
            )
        ).also { functions ->
            logger.warn(" Found functions ${functions.size}: ${functions}")
            functions.forEach { function ->
                logger.warn(" Found function: $function")
            }
        }
        logger.warn(" Visiting dependency module: ${moduleFragment.descriptor.name}")
        moduleFragment.descriptor.getPackage(FqName("enro_generated_bindings"))
            .fragments
            .also { logger.warn(" Found ${it} fragments")}
            .forEach { it.acceptVoid(Visitor()) }
    }

    inner class Visitor : DeclarationDescriptorVisitorEmptyBodies<Void, Void>() {

        override fun visitPackageViewDescriptor(
            descriptor: PackageViewDescriptor?,
            data: Void?
        ): Void? {
            logger.warn("    Visiting package: ${descriptor?.name}")
            descriptor?.acceptVoid(this)
            return super.visitPackageViewDescriptor(descriptor, data)
        }

        override fun visitClassDescriptor(
            descriptor: ClassDescriptor?,
            data: Void?
        ): Void? {
            logger.warn("    Visiting class: ${descriptor?.name}")
            return null
        }
    }

    inner class Irv : IrVisitorVoid() {
        override fun visitElement(element: IrElement) {
            when (element) {
                is IrDeclaration,
                is IrFile,
                is IrModuleFragment -> element.acceptChildrenVoid(this)
                else -> {}
            }
        }

        override fun visitClass(
            declaration: IrClass,
            data: Nothing?
        ) {
            logger.warn("    Visiting class: ${declaration.name}")
        }

        override fun visitClass(declaration: IrClass) {
            logger.warn("    Visiting class: ${declaration.name}")
        }
    }
}
