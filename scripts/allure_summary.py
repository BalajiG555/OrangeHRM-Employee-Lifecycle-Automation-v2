#!/usr/bin/env python3
"""Prints a Markdown summary of a generated Allure report (for $GITHUB_STEP_SUMMARY).

Usage: allure_summary.py ALLURE_REPORT_DIR [--fail-if-empty]

--fail-if-empty exits with status 1 when no tests were executed at all, so a pipeline that
selected zero scenarios across every shard can never show up as green.
"""
import json
import sys
from pathlib import Path


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    fail_if_empty = "--fail-if-empty" in sys.argv
    report_dir = Path(args[0] if args else "allure-report")
    summary_file = report_dir / "widgets" / "summary.json"
    if not summary_file.exists():
        print("## Allure report\n\nNo summary available (no results were produced).")
        sys.exit(1 if fail_if_empty else 0)
    summary = json.loads(summary_file.read_text(encoding="utf-8"))
    stats = summary.get("statistic", {})
    duration_ms = (summary.get("time") or {}).get("duration", 0) or 0
    total = stats.get("total", 0)
    passed = stats.get("passed", 0)
    rate = f"{(passed / total * 100):.1f}%" if total else "n/a"
    print("## Allure report")
    print()
    print("| Total | Passed | Failed | Broken | Skipped | Pass rate | Duration |")
    print("|---|---|---|---|---|---|---|")
    print(f"| {total} | {passed} | {stats.get('failed', 0)} | {stats.get('broken', 0)} | "
          f"{stats.get('skipped', 0)} | {rate} | {duration_ms / 1000:.0f}s |")
    print()
    print("Full HTML report: download the `allure-report` artifact or open the GitHub Pages site.")
    if fail_if_empty and total == 0:
        print("\n**ERROR: no scenarios were executed in any shard - check the tag expression.**")
        sys.exit(1)


if __name__ == "__main__":
    main()
