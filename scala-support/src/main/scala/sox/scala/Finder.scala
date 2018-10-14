package sox.scala

import sox.command.{AbstractCommand, AbstractContext, ReflectiveCommandManager}
import sox.inject.Injector

import scala.reflect.runtime.universe

private[scala] class Finder[C <: AbstractContext[C], T <: AbstractCommand[C, T]] extends ReflectiveCommandManager.SubcommandFinder[C, T] {
    private type JavaStream[U] = java.util.stream.Stream[U]

    override def findSubCommands(manager: ReflectiveCommandManager[_, C, T],
                                 injector: Injector, command: T): JavaStream[T] = {
        //cheat compiler, otherwise we'd get something like "type arguments
        //do not conform to class type parameter bounds"
        val c: AbstractCommand[C, T] = command.asInstanceOf[AbstractCommand[C, T]]

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
            .map(_.asInstanceOf[T])
            .asJavaCollection
            .stream()
    }
}
