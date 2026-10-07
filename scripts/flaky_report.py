#!/usr/bin/env python3
"""Flaky-test detection across repeated runs.

Reads one Cucumber JSON report per run (any directory layout below RUNS_DIR) and classifies every scenario:

* STABLE PASS     - passed in every run
* FLAKY           - passed in some runs and failed in others (same code, same data -> non-deterministic)
* ALWAYS FAILING  - failed in every run (a real defect or broken test, not flakiness)

Usage: flaky_report.py RUNS_DIR [--markdown out.md] [--json out.json]
"""
import argparse
import json
import sys
from collections import defaultdict
from pathlib import Path

FAILING_STATUSES = {"failed", "undefined", "ambiguous"}


def scenario_status(element):
    """Collapse hooks + steps into one status: failed > skipped > passed."""
    results = []
    for section in ("before", "steps", "after"):
        for item in element.get(section, []) or []:
            results.append((item.get("result") or {}).get("status", "unknown"))
    if any(status in FAILING_STATUSES for status in results):
        return "failed"
    if any(status in ("skipped", "pending") for status in results):
        return "skipped"
    return "passed"


def collect(runs_dir):
    outcomes = defaultdict(list)
    report_files = sorted(Path(runs_dir).rglob("*.json"))
    for report in report_files:
        try:
            features = json.loads(report.read_text(encoding="utf-8") or "[]")
        except json.JSONDecodeError:
            print(f"warning: skipping unreadable report {report}", file=sys.stderr)
            continue
        for feature in features:
            uri = feature.get("uri", "unknown")
            for element in feature.get("elements", []):
                if element.get("type") == "background":
                    continue
                key = f"{element.get('name', '?')} [{uri.split('/')[-1]}:{element.get('line', '?')}]"
                outcomes[key].append(scenario_status(element))
    return outcomes, len(report_files)


def classify(statuses):
    executed = [s for s in statuses if s != "skipped"]
    if not executed:
        return "SKIPPED"
    failed = executed.count("failed")
    if failed == 0:
        return "STABLE PASS"
    if failed == len(executed):
        return "ALWAYS FAILING"
    return "FLAKY"


def build(outcomes, run_count):
    rows = []
    for name, statuses in outcomes.items():
        executed = [s for s in statuses if s != "skipped"]
        failed = executed.count("failed")
        rows.append({
            "scenario": name,
            "classification": classify(statuses),
            "runs": len(executed),
            "failures": failed,
            "failure_rate": round(failed / len(executed), 2) if executed else 0.0,
        })
    order = {"FLAKY": 0, "ALWAYS FAILING": 1, "STABLE PASS": 2, "SKIPPED": 3}
    rows.sort(key=lambda r: (order[r["classification"]], -r["failure_rate"], r["scenario"]))
    return {"runs_analysed": run_count, "scenarios": rows}


def to_markdown(report):
    flaky = [r for r in report["scenarios"] if r["classification"] == "FLAKY"]
    failing = [r for r in report["scenarios"] if r["classification"] == "ALWAYS FAILING"]
    lines = [
        "## Flaky test detection",
        "",
        f"Runs analysed: **{report['runs_analysed']}** | Scenarios: **{len(report['scenarios'])}** | "
        f"Flaky: **{len(flaky)}** | Always failing: **{len(failing)}**",
        "",
        "| Scenario | Classification | Failures / Runs | Failure rate |",
        "|---|---|---|---|",
    ]
    for row in report["scenarios"]:
        lines.append(f"| {row['scenario']} | {row['classification']} | {row['failures']} / {row['runs']} "
                     f"| {int(row['failure_rate'] * 100)}% |")
    lines += ["", "Action: tag FLAKY scenarios `@quarantine`, raise a ticket, fix the root cause, then "
              "remove the tag once they are stable again in this report.", ""]
    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("runs_dir")
    parser.add_argument("--markdown")
    parser.add_argument("--json")
    args = parser.parse_args()

    outcomes, run_count = collect(args.runs_dir)
    report = build(outcomes, run_count)
    markdown = to_markdown(report)
    if args.markdown:
        Path(args.markdown).write_text(markdown, encoding="utf-8")
    if args.json:
        Path(args.json).write_text(json.dumps(report, indent=2), encoding="utf-8")
    print(markdown)


if __name__ == "__main__":
    main()
