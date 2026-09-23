# Security Policy

## Supported Versions

Security fixes are applied to the latest version of NetShield-Pro-IDS on the `main` branch.

Older releases may not receive security updates.

## Reporting a Vulnerability

Please do not disclose security vulnerabilities publicly in GitHub Issues.

If you discover a potential vulnerability, use GitHub's private vulnerability reporting feature for this repository when available.

When reporting a vulnerability, please include:

- A clear description of the issue
- Steps to reproduce it
- The affected component or file
- The potential security impact
- Relevant logs or screenshots, with passwords, keys, tokens, and personal information removed
- Any suggested mitigation, if known

Please allow reasonable time for investigation and remediation before publicly disclosing the issue.

## Security-Sensitive Information

Never commit:

- Private keys
- Keystores
- Passwords
- API keys
- Access tokens
- Firebase service credentials
- `.env` files containing secrets
- Personal or production network credentials

The repository's `.gitignore` is configured to exclude common local signing keys, environment files, Gradle files, IDE files, and build output.

## Scope

NetShield-Pro-IDS is a security and network-monitoring project. Security findings involving the application, its dependencies, build configuration, network communication, authentication, encryption, or data handling are relevant to this policy.

## Disclaimer

NetShield-Pro-IDS is provided for security research, monitoring, and defensive purposes. Users are responsible for using the software lawfully and for securing their own systems and credentials.
