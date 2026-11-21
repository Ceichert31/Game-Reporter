# Game-Reporter
A bug report API that integrates with Discord to send player in-game bug reports to a specified Discord channel. 

## Setup

Game Reporter is meant to be easy for anyone to set up. If you run into any issues in the setup process, post a question in [Discussion](https://github.com/Ceichert31/Game-Reporter/discussions).

1. Fork repo
2. Setup Render and connect repo
3. Set Discord webhook URL in environment file

## Hosting
[Hosting Page](https://github.com/Ceichert31/Game-Reporter/wiki/Hosting)

## Implementation
### C# Implementation
```C#
using System;
using System.Net.Http;
using System.Net.Http.Json;
using System.Threading.Tasks;


public class BugReport
{
	public string Title { get; set; }
	public string Message { get; set; }
}

/// Handles sending HTTP POST requests to the reporter API
public class Reporter
{
	private string reportUrl = "https://<your-render-domain>.onrender.com"
	private readonly HttpClient client = new HttpClient();

	private async Task SubmitBugReport(report BugReport)
	{
		try
		{
			var response = await client.PostAsJsonAsync(reportUrl, report);

			response.EnsureSuccessStatusCode();
		}
		catch (HttpRequestException e)
		{
			Console.WriteLine($"Failed to submit bug report: {e.Message}");
		}
	}
}
```
### GDScript Implementation
```GDScript
var _report_url := "https://<your-render-domain>.onrender.com"

func submit_bug_report(title: String, message: String) -> void:
  var report = {
    "title": title,
    "message": message
  }

  var json_string = JSON.stringify(report)
  var headers = ["Content-Type: application/json"]
  var response = http_request.request(
		url, 
		headers, 
		HTTPClient.METHOD_POST, 
		json_string
	)

  if response != OK:
		push_error("An error occurred while trying to submit the bug report. Please check your internet connection and try again.")
```

# ToDo
- [ ] Setup screenshot support
- [ ] Setup log file/game state support
- [ ] Create guide for setting up Render + integration into game
