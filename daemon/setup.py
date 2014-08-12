from os import chdir
from os.path import dirname

try:
    from setuptools import setup, find_packages
except ImportError:
    # setuptools wasn't available, so install and try again
    from ez_setup import use_setuptools
    use_setuptools()
    from setuptools import setup, find_packages

from daemon import get_package_version, DAEMON_MAIN

root_dir = dirname(__file__)

if root_dir != "":
    chdir(root_dir)

setup(
    name='daemon',
    version=get_package_version(),
    author='Matt Maclean',
    author_email='matthewcmaclean@gmail.com',
    description='Sends information from code editors to server',
    entry_points={
        'console_scripts': {
            DAEMON_MAIN + ' = daemon.cli:main',
        }
    },
    packages=find_packages(exclude="test"),
    include_package_data=True,
    install_requires=[
        'Envoy>=0.0.2',
        'requests>=2.3.0',
    ],
    test_suite='unittest2.collector',
    tests_require=[
        'unittest2>=0.5.1',
        'pep8>=1.5.7',
        'gitpython>=0.3.1',
    ],
)
