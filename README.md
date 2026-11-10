**Tienda de informática**

Proyecto de portfolio: una tienda online de componentes y periféricos informáticos, inspirada en PcComponentes.

**Funcionalidades**
- Catálogo de artículos con marcas, categorías y subcategorías (opcional)
- Dos tipos de usuario:
  - **Comprador**: Ve el catálogo, simula compras de varios artículos, y puede comentar los artículos. (En caso de que los haya comprado saldrá como compra verificada)
  - **Administrador**: Añade artículos y crea categorías, que aparecen automáticamente en los menús
- Un usuario puede tener ambos roles

**Tecnologías**

Java 25 · Spring Boot 4.1 · Spring Security · Hibernate/JPA · AspectJ · PostgreSQL · Maven · Lombok

**Modelo de datos**
```  
erDiagram  
   CATEGORIA ||--o{ SUBCATEGORIA : contiene  
   CATEGORIA ||--o{ ARTICULO : clasifica  
   SUBCATEGORIA |o--o{ ARTICULO : "organiza (opcional)"  
   MARCA ||--o{ ARTICULO : identifica  
   ARTICULO ||--o{ DETALLE_COMPRA : incluido  
   COMPRA ||--|{ DETALLE_COMPRA : contiene  
   USUARIO ||--o{ COMPRA : realiza  
   USUARIO ||--|{ USUARIO_ROL : tiene  
   ROL ||--o{ USUARIO_ROL : asignado  
   ARTICULO ||--o{ COMENTARIO : recibe  
   USUARIO ||--o{ COMENTARIO : escribe  
   
   CATEGORIA {  
     bigint id_categoria PK  
     varchar nombre UK  
   }  
   SUBCATEGORIA {  
     bigint id_subcategoria PK  
     bigint id_categoria FK  
     varchar nombre  
   }  
   MARCA {  
     bigint id_marca PK  
     varchar nombre UK  
   }  
   ARTICULO {  
     bigint id_articulo PK  
     bigint id_marca FK  
     bigint id_categoria FK  
     bigint id_subcategoria FK  
     varchar nombre  
     text descripcion  
     varchar imagen_url  
     numeric precio  
     integer stock  
     boolean activo  
   }  
   USUARIO {  
     bigint id_usuario PK  
     varchar nombre  
     varchar correo UK  
     varchar password_hash  
   }  
   ROL {  
     bigint id_rol PK  
     varchar nombre UK  
   }  
   USUARIO_ROL {  
     bigint id_usuario PK  
     bigint id_rol PK  
   }  
   COMPRA {  
     bigint id_compra PK  
     bigint id_usuario FK  
     timestamp fecha  
     varchar estado  
     numeric total  
   }  
   DETALLE_COMPRA {  
     bigint id_detalle PK  
     bigint id_compra FK  
     bigint id_articulo FK  
     integer cantidad  
     numeric precio_unitario  
   }  
   COMENTARIO {  
     bigint id_comentario PK  
     bigint id_usuario FK  
     bigint id_articulo FK  
     integer valoracion  
     text texto  
     timestamp fecha  
     boolean visible  
   }  
   
```  
