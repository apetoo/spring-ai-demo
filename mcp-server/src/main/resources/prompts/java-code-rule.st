java远程调用规范,使用FeignClient,需要让用户告知注册名,contextId为注册名+Id即可

```java
@Api(tags = "接口描述")
@FeignClient(name = "", contextId = "")
@RequestMapping("/template")
public interface TemplateApi {
...
}
```
