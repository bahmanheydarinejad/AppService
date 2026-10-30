$lines = Get-Content -LiteralPath "c:\Users\parse-it\Documents\Git\AppService\AGENTS.md" -Encoding UTF8

$headers = @{}
$tocLinks = @()
$inToc = $false

foreach ($line in $lines) {
    if ($line -like "*Table of Contents*") {
        $inToc = $true
        continue
    }
    if ($inToc -and $line.StartsWith("---")) {
        $inToc = $false
    }
    if ($inToc) {
        if ($line -match '\[(.*?)\]\(#(.*?)\)') {
            $tocLinks += [PSCustomObject]@{
                Text = $matches[1]
                Slug = $matches[2]
            }
        }
    }
    if ($line.StartsWith("#")) {
        # Extract header title after '#'
        $hText = $line -replace '^#+\s*', ''
        $hText = $hText.Trim()
        $slug = $hText.ToLower()
        $slug = $slug -replace '[^\w\s-]', ''
        $slug = $slug -replace '\s+', '-'
        $slug = $slug.Trim('-')
        $headers[$slug] = $hText
    }
}

Write-Host "Total Document Headers Count: $($headers.Count)"
Write-Host "Total TOC Links Count: $($tocLinks.Count)"

$failed = 0
foreach ($link in $tocLinks) {
    $s = $link.Slug
    if ($headers.ContainsKey($s)) {
        Write-Host "[OK] #$s matches '$($headers[$s])'"
    } else {
        Write-Host "[FAIL] #$s (Text: $($link.Text)) has no matching header"
        $failed++
    }
}

if ($failed -eq 0) {
    Write-Host "`nSUCCESS: All $($tocLinks.Count) TOC links match headers!"
} else {
    Write-Host "`nFAILURE: $failed links failed to match."
    exit 1
}
