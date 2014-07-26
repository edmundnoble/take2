import sys

try:
    from setuptools import setup, find_packages
except ImportError:
    # setuptools wasn't available, so install and try again
    from ez_setup import use_setuptools
    use_setuptools()
    from setuptools import setup, find_packages

from daemon import get_package_version, DAEMON_MAIN

setup(
    name=DAEMON_MAIN,
    version=get_package_version(),
    author='Matt Maclean',
    author_email='matthewcmaclean@gmail.com',
    description='Sends information from code editors to server',
    entry_points={
        'console_scripts': {
            DAEMON_MAIN + ' = daemon.accio:main',
        }
    },
    packages=find_packages(),
    include_package_data=True,
    )
