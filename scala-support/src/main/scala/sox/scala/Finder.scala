package sox.scala

import sox.command.{AbstractCommand, AbstractContext, ReflectiveCommandManager}
import sox.inject.Injector

import scala.reflect.runtime.universe

private[scala] class Finder[C <: AbstractContext[C]] extends ReflectiveCommandManager.SubcommandFinder[C] {
    private type JavaStream[T] = java.util.stream.Stream[T]

    override def findSubCommands(manager: ReflectiveCommandManager[_, C],
                                 injector: Injector, command: AbstractCommand[_ <: C]): JavaStream[AbstractCommand[C]] = {
        //cheat compiler, otherwise we'd get something like "type arguments
        //do not conform to class type parameter bounds"
        val c: AbstractCommand[C] = command.asInstanceOf[AbstractCommand[C]]

        val runtimeMirror = universe.runtimeMirror(c.getClass.getClassLoader)
        val instanceMirror = runtimeMirror.reflect(c)

        import collection.JavaConverters._

        instanceMirror.symbol.info.decls
            .filter(_.isClass).map(_.asClass)
            .filter(c=>manager.commandClass().isAssignableFrom(runtimeMirror.runtimeClass(c)))
            .filter(c=> !(c.isAbstract || c.isSynthetic || c.isTrait))
            .map(c => {
                c.info.decls.filter(_.isConstructor).map(_.asMethod).find(_.paramLists.head.isEmpty) match {
                    case None => throw new UnsupportedOperationException("Constructors with arguments not supported yet")
                    case Some(constructor) => instanceMirror.reflectClass(c).reflectConstructor(constructor)()
                }
            })
            .map(_.asInstanceOf[AbstractCommand[C]])
            .asJavaCollection
            .stream()
    }
}
