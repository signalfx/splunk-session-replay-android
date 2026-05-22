# Session Replay SDK for Splunk Observability

## Documentation
- [Install the Splunk RUM Android Agent](https://help.splunk.com/en/splunk-observability-cloud/manage-data/instrument-front-end-applications/instrument-mobile-and-web-applications-for-splunk-real-user-monitoring-rum/instrument-android-applications-for-splunk-rum/splunk-rum-android-agent-version-2.0.0-and-above/install-the-splunk-rum-android-agent)
- [Record Android Sessions](https://help.splunk.com/en/splunk-observability-cloud/monitor-end-user-experience/real-user-monitoring/replay-user-sessions/record-android-sessions)

## Overview
The SDK provides session replay recording capabilities for Android applications integrated with Splunk Real User Monitoring (RUM). It captures user interactions and UI state to help developers investigate crashes, performance issues, and user experience problems.

## Repository Structure

The Session Replay SDK is part of the broader Splunk Android Observability ecosystem:

- [`splunk-otel-android`](https://github.com/signalfx/splunk-otel-android)  
  Core OpenTelemetry-based Android instrumentation and RUM SDK.

- [`splunk-session-replay-android`](https://github.com/signalfx/splunk-session-replay-android)  
  Session Replay SDK for capturing and replaying Android user sessions.

- [`splunk-common-android`](https://github.com/signalfx/splunk-common-android)  
  Shared Android components and utilities used across Splunk Android SDKs.