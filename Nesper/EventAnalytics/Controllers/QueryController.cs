using Microsoft.AspNetCore.Mvc;

namespace EventAnalytics.Controllers;

[ApiController]
[Route("api/[controller]/[action]")]
public class QueryController : ControllerBase
{
    private readonly ILogger<QueryController> logger;

    public QueryController(ILogger<QueryController> logger) => this.logger = logger;

    [HttpGet]
    public void Do()
    {
    }
}
