const fs = require('fs');
const path = require('path');

const archPath = path.resolve('c:/Users/parse-it/Documents/Git/AppService/ARCHITECTURE.md');
if (!fs.existsSync(archPath)) {
  console.error('File not found:', archPath);
  process.exit(1);
}

const content = fs.readFileSync(archPath, 'utf8');

// 1. Extract all TOC links: - [Text](#anchor)
const tocRegex = /- \[([^\]]+)\]\(#([^\)]+)\)/g;
const tocLinks = [];
let match;
while ((match = tocRegex.exec(content)) !== null) {
  tocLinks.push({ text: match[1], anchor: match[2] });
}

console.log('Total TOC links found:', tocLinks.length);

// 2. Extract all explicit HTML anchors: <a id="..."></a>
const explicitAnchorRegex = /<a id="([^"]+)"><\/a>/g;
const explicitAnchors = new Set();
while ((match = explicitAnchorRegex.exec(content)) !== null) {
  explicitAnchors.add(match[1]);
}
console.log('Explicit <a id="..."> anchors found:', explicitAnchors.size);

// 3. Extract all Markdown headings
const lines = content.split('\n');
const headings = [];
for (const line of lines) {
  const hMatch = line.match(/^(#{1,6})\s+(.+)$/);
  if (hMatch) {
    headings.push({ level: hMatch[1].length, title: hMatch[2].trim() });
  }
}
console.log('Total Markdown Headings found:', headings.length);

// 4. Compute GitHub slug
function githubSlug(text) {
  return text
    .toLowerCase()
    .trim()
    .replace(/[^\w\s-]/g, '')
    .replace(/\s+/g, '-');
}

const headingSlugs = new Set(headings.map(h => githubSlug(h.title)));

// 5. Verify every TOC link
let missingCount = 0;
for (const item of tocLinks) {
  const inExplicit = explicitAnchors.has(item.anchor);
  const inHeadings = headingSlugs.has(item.anchor);
  if (!inExplicit && !inHeadings) {
    console.error('MISSING ANCHOR for TOC link: "' + item.text + '" => #' + item.anchor);
    missingCount++;
  } else {
    console.log('VERIFIED: #' + item.anchor + ' (Explicit: ' + inExplicit + ', Heading Slug: ' + inHeadings + ')');
  }
}

if (missingCount === 0) {
  console.log('\n========================================');
  console.log('SUCCESS: All ' + tocLinks.length + ' TOC links match valid headings/anchors in ARCHITECTURE.md!');
  console.log('========================================');
} else {
  console.error('\nFAILURE: ' + missingCount + ' anchor links failed verification.');
  process.exit(1);
}
