python远程调用规范,使用requests模块

```python
import requests

def get_template(id):
    url = ""
    params = {"id": id}
    response = requests.get(url, params=params)
    return response.json()
```
