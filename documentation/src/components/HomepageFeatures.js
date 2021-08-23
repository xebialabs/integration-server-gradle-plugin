import React from 'react';
import clsx from 'clsx';
import styles from './HomepageFeatures.module.css';

const FeatureList = [
  {
    title: 'Easy to Use',
    Svg: require('../../static/img/1.svg').default,
    description: (
      <>
        Integration Server was designed from the ground up to be easily installed and
        used to get your integration tests against Deploy up and running quickly.
      </>
    ),
  },
  {
    title: 'Focus on What Matters',
    Svg: require('../../static/img/2.svg').default,
    description: (
      <>
        Integration Server lets you focus on your test scenarios, and we&apos;ll do the chores to setup the environment.
          Go ahead and try it out.
      </>
    ),
  },
  {
    title: 'Backed by Digital.ai',
    Svg: require('../../static/img/3.svg').default,
    description: (
      <>
        Extend or customize your Deploy installation for integration testing. You can run server,
          workers, satellite, customize your configuration, run your setup from a docker container.
      </>
    ),
  },
];

function Feature({Svg, title, description}) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} alt={title} />
      </div>
      <div className="text--center padding-horiz--md">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
