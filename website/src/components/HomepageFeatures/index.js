import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';

const FeatureList = [
  {
    title: 'Portable cloud service',
    img: require('@site/static/img/cloud.png').default,
    description: (
      <>
        An Open Services Cloud service is portable. It means that it can be
        deployed on any cloud provider (supporting Open Services Cloud).
        You don't have any cloud provider lock-in anymore.
      </>
    ),
  },
  {
    title: 'Managed service easily',
    img: require('@site/static/img/easily.png').default,
    description: (
      <>
        Creating a service with Open Services Cloud is very easy: you describe a
        service using a configuration language, with the integration with other
        cloud provider services.
        You don't have service lock out anymore.
      </>
    ),
  },
  {
    title: 'Open Source services catalog',
    img: require('@site/static/img/catalog.png').default,
    description: (
      <>
        As a service is described using Open Services Cloud Configuration Language,
        it's pretty easy to provide a catalog of services descriptors, running on
        any cloud.
      </>
    ),
  },
];

function Feature({img, title, description}) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <img src={img} width="100"/>
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
