# DataChannel 相关设计

内部持有一个 **DataBuffer**，所有访问操作都需要经过 Buffer 来执行

------

当用户没有调用 **InsertableDataChannel** 或 **DeletableDataChannel** 的相关方法时，**Buffer** 仅仅是用来过渡的 **FileBuffer** 或 **NioBuffer**，否则当涉及到插入或者删除操作时需要切换到 [**PieceBuffer**](piece-buffer.zh.md).



